(ns main
  (:require 
    [texts :as texts]
    [gamestate :as gs]
    [kjv :as kjv]
    [utils :as u]))

(defn create-canvas! [id]
  (let [id-string (str "#canvas_" id)
        try-canv (js/document.querySelector id-string)
        canv (if try-canv
               try-canv
               (js/document.createElement "canvas"))
        w (.-innerWidth js/window)
        h (.-innerHeight js/window)
        ratio (.-devicePixelRatio js/window)] 
    (set! (.-width canv) (* w ratio))
    (set! (.-height canv) (* h ratio))
    (set! (-> canv .-style .-width) (str w "px"))
    (set! (-> canv .-style .-height) (str h "px"))
    (set! (.-id canv) id-string)
    canv))

(defn create-context! [canvas]
  (let [context (canvas.getContext "2d")]
    (set! (.-imageSmoothingEnabled context) false)
    context))

(defn resize-canvas! [canvas context ratio]
  (let [w (.-innerWidth js/window)
        h (.-innerHeight js/window)]
    (set! (.-width canvas) (* w ratio))
    (set! (.-height canvas) (* h ratio))
    (set! (-> canvas .-style .-width) (str w "px"))
    (set! (-> canvas .-style .-height) (str h "px"))
    (set! gs/game-state.textwidth (.min js/Math (:MAXTEXTWIDTH gs/constants) (- w 100)))
    (.scale context ratio ratio)
    (texts/remeasure-text-nodes!)))
    
(defn update-camera-pos! []
  (let [targX (:xpos (nth (:texts gs/game-state) (:targetnode gs/game-state)))
        targY (:ypos (nth (:texts gs/game-state) (:targetnode gs/game-state)))
        cX (:cameraX gs/game-state)
        cY (:cameraY gs/game-state)
        dX (- targX cX) 
        dY (- targY cY) 
        dist (.sqrt js/Math (+ (* dX dX) (* dY dY))) ;Distance to target
        dt (:dt gs/game-state)  ;Delta time (time since last frame)
        smoothing 3.0        ;Controls deceleration speed
        threshold 0.5        ;Minimum distance to stop movement
        factor (- 1.0 (js/Math.exp (* (- smoothing) dt))) ;; Exponential smoothing
        new-cX (+ cX (* factor dX))
        new-cY (+ cY (* factor dY))]
    ;Clamp movement when very close to prevent infinite movement
    (if (< dist threshold)
      (do
        (set! gs/game-state.cameraX targX)
        (set! gs/game-state.cameraY targY))
      (do
        (set! gs/game-state.cameraX new-cX)
        (set! gs/game-state.cameraY new-cY)))))

(defn main [ftime] ;frame time
  (let [ctx (:context gs/game-state)
        canvas (:canvas gs/game-state)]
    (set! gs/game-state.dt (/ (- ftime (:lastUpdate gs/game-state)) 1000))
    (set! gs/game-state.lastUpdate ftime)
    (update-camera-pos!) 
    (ctx.clearRect 0 0 (.-width canvas) (.-height canvas))
    (texts/set-text-info!)
    ;diagnostics
    (.fillText ctx (/ (:dt gs/game-state)) 50 30)
    (.fillText ctx (:activeword gs/game-state) 50 50)
    (.fillText ctx (:targetnode gs/game-state) 50 70)
    ;toggle active words
    (when (not (:mousedown gs/game-state))
      (set! (.-activeword gs/game-state) nil))
    (set! ctx.globalAlpha 0.5)
    ;draw text nodes
    (doseq [[idx element] (map-indexed vector (:texts gs/game-state)) 
            [widx word]    (map-indexed vector (.-words (.-drawdata element)))]
      (if (not= (.-targetnode gs/game-state) idx)
        (set! ctx.globalAlpha 0.2)
        (set! ctx.globalAlpha 1))
      (let [wordx (.-xpos word)
            wordy (.-ypos word)
            xpos (u/world->screen :x (+ (.-xpos element) wordx))
            ypos (u/world->screen :y (+ (.-ypos element) wordy))]
        (if (u/point-in-rect? (:mouseX gs/game-state) (:mouseY gs/game-state)
                            xpos ypos (.-width word) (.-height word))
          (when (not (:mousedown gs/game-state))
             (set! (.-targetnode gs/game-state) idx)
             (set! (.-activeword gs/game-state) [idx widx])
             (set! ctx.fillStyle "red"))
          (set! ctx.fillStyle "black"))
        (when (u/on-screen? xpos ypos (.-width word) (.-height word))
          (.fillText ctx (:word word) xpos ypos))))
    ;draw current line
    (when (and (.-activeword gs/game-state) (.-mousedown gs/game-state))
      (.beginPath ctx)
      (.moveTo ctx (.-strokestartX gs/game-state) (.-strokestartY gs/game-state))
      (.lineTo ctx (.-mouseX gs/game-state) (.-mouseY gs/game-state))
      (.stroke ctx))
    ;draw strokes. TODO: optimize for onscreen lol
    (set! ctx.fillStyle "black")
    (set! ctx.globalAlpha 0.2)
    (doseq [stroke (.-strokes gs/game-state)]
      (let [sx (u/world->screen :x (.-sx stroke))
            sy (u/world->screen :y (.-sy stroke))
            ex (u/world->screen :x (.-ex stroke))
            ey (u/world->screen :y (.-ey stroke))
            string (.-string stroke)
            angle (.atan2 js/Math (- ey sy) (- ex sx))]
        ;draw line
        (.beginPath ctx)
        (.moveTo ctx sx sy) 
        (.lineTo ctx ex ey) 
        (.stroke ctx)
        ;draw text
        (.save ctx)
        (.translate ctx sx sy)
        (.rotate ctx angle)
        (.fillText ctx string 0 0)
        (.restore ctx)))
    (js/window.requestAnimationFrame main)))

(defn markovfy-kjv! []
  (let [words (.split kjv/kjv #"\s")
        table (.-transitiontable gs/game-state)
        pairs (partition 2 1 words)]
    (doseq [[word1 word2] pairs]
      (let [inner-map (if (.has table word1) (.get table word1) (js/Map.))] ;; Get existing map or create new one
        (.set inner-map word2 (if (.has inner-map word2)
                               (inc (.get inner-map word2))
                               1))
        (.set table word1 inner-map)))))

(defn weighted-random-choice [weights]
  (if weights
   (let [entries (js/Array.from (.entries weights))
         total (reduce + (map second entries))]
      (when (pos? total)
        (let [r (* (.random js/Math) total)
              data {:cumulative 0
                    :index 0}]
          (loop []
            (if (>= (.-index data) (.-length entries))
              nil
              (let [[k v] (.at entries (.-index data))]
                (if (>= (+ (.-cumulative data) v) r)
                  k
                  (do
                    (set! (.-cumulative data) (+ (.-cumulative data) v))
                    (set! (.-index data) (inc (.-index data)))
                    (recur)))))))))
   ". But"))

(defn markov-chain [seed dist]
  (let [ctx (.-context gs/game-state)]
    (println dist)
    (loop [output [seed]
           current seed
           width (texts/word-width (.measureText ctx (.join output " ")))]
      #_(println width)
      (if (< width dist)
         (let [nextword (weighted-random-choice 
                          (.get (.-transitiontable gs/game-state) current))
               nextarray (conj output nextword)
               nextwidth (texts/word-width (.measureText ctx (.join nextarray " ")))]
           (recur nextarray
                  nextword
                  nextwidth))
         (.join output " ")))))

(defn load-kjv! []
  (let [lines (.split kjv/kjv "\n")
        choice (rand-nth lines)]
    (markovfy-kjv!)
    (doseq [[idx line] (map-indexed vector lines)
            word (.split line #"\s")]
      (let [table (.-wordindices gs/game-state)
            lineset (if (.has table word)
                      (.get table word)
                      (js/Set.))]
        (.add lineset idx)
        (.set table word lineset)))
    (set! (.-lines gs/game-state) lines)
    (.push (.-texts gs/game-state) (texts/create-text-node choice 0 0))))

(defn begin-stroke! [e]
  (when (.-activeword gs/game-state)
      (set! (.-strokestartX gs/game-state) (.-clientX e))
      (set! (.-strokestartY gs/game-state) (.-clientY e))))

(defn end-stroke! [e]
  (let [sxpos (u/screen->world :x (.-strokestartX gs/game-state))
        sypos (u/screen->world :y (.-strokestartY gs/game-state))
        expos (u/screen->world :x (.-clientX e))
        eypos (u/screen->world :y (.-clientY e))
        active (.-activeword gs/game-state)]
    (when active 
      (let [[idx widx] active
            wordobj (-> gs/game-state
                         (.-texts)
                         (nth idx)
                         (.-drawdata)
                         (.-words)
                         (nth widx))
            word (.-word wordobj)
            dist (u/distance-between sxpos sypos expos eypos)
            words (markov-chain word dist)
            lastword (last (.split words #"\s"))
            availlines (.get (.-wordindices gs/game-state) lastword)
            lineidx (rand-nth (js/Array.from availlines))
            nextline (nth (.-lines gs/game-state) lineidx)]
        (.push (.-texts gs/game-state) (texts/create-text-node nextline expos eypos))
        (.push (.-strokes gs/game-state) {:sx sxpos :sy sypos :ex expos :ey eypos :string words})
        (set! (.-targetnode gs/game-state) (dec (count (.-texts gs/game-state))))))))

(defn cancel-stroke! []
  (set! (.-activeword gs/game-state) nil))

(defn init []
  (let [canvas  (create-canvas! "canv")   ;create canvas
        context (create-context! canvas)
        ratio   (.-devicePixelRatio js/window)
        _       (resize-canvas! canvas context ratio)]
    (set! gs/game-state.canvas canvas)
    (set! gs/game-state.context context)
    (-> (js/document.querySelector "#app")
        (.append canvas))
    (.addEventListener js/window "resize" (partial resize-canvas! canvas context ratio) false)
    (.addEventListener canvas "mouseleave" (fn [_] 
                                             (cancel-stroke!)
                                             (set! gs/game-state.mousedown false)))
    (.addEventListener canvas "mouseup" (fn [e]
                                          (end-stroke! e)
                                          (set! gs/game-state.mousedown false)))
    (.addEventListener canvas "mousedown" (fn [e] 
                                            (begin-stroke! e)
                                            (set! gs/game-state.mousedown true)))
    (.addEventListener canvas "mousemove" (fn [{:keys [offsetX offsetY]}]
                                            (set! gs/game-state.mouseX offsetX)
                                            (set! gs/game-state.mouseY offsetY)))
    (load-kjv!)
    (js/window.requestAnimationFrame main)))
    

(init)

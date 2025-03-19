(ns main
  (:require 
    [texts :as texts]
    [gamestate :as gs]
    [kjv :as kjv]))

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
    

(defn point-in-rect? 
  "checks if the point px py 
  is in rectangle x y w h" 
  [px py x y w h]
  (and (> px x)
       (> py y)
       (< px (+ x w))
       (< py (+ y h))))

(defn on-screen? [x y w h]
  (let [bleft  x
        btop   y
        bright (+ x w)
        bbot   (+ y h)]
    (and (> bright 0)
         (< bleft (.-innerWidth js/window))
         (> bbot 0)
         (< btop (.-innerHeight js/window))))) 

(defn world->screen [x-or-y coord]
  (if (= x-or-y :x)
    (+ (/ (.-innerWidth js/window) 2) (- coord (.-cameraX gs/game-state)))
    (+ (/ (.-innerHeight js/window) 2) (- coord (.-cameraY gs/game-state)))))

(defn screen->world [x-or-y coord]
  (if (= x-or-y :x)
    (+ (.-cameraX gs/game-state) (- coord (/ (.-innerWidth js/window) 2)))
    (+ (.-cameraY gs/game-state) (- coord (/ (.-innerHeight js/window) 2)))))

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
    ;draw text nodes
    (doseq [[idx element] (map-indexed vector (:texts gs/game-state)) 
            word    (.-words (.-drawdata element))]
      (if (not= (.-targetnode gs/game-state) idx)
        (set! ctx.globalAlpha 0.5)
        (set! ctx.globalAlpha 1))
      (let [wordx (.-xpos word)
            wordy (.-ypos word)
            xpos (world->screen :x (+ (.-xpos element) wordx))
            ypos (world->screen :y (+ (.-ypos element) wordy))]
        (if (point-in-rect? (:mouseX gs/game-state) (:mouseY gs/game-state)
                            xpos ypos (.-width word) (.-height word))
          (when (not (:mousedown gs/game-state))
             (set! (.-targetnode gs/game-state) idx)
             (set! (.-activeword gs/game-state) [idx word])
             (set! ctx.fillStyle "red"))
          (set! ctx.fillStyle "black"))
        (when (on-screen? xpos ypos (.-width word) (.-height word))
          (.fillText ctx (:word word) xpos ypos))))
    ;draw current line
    (when (and (.-activeword gs/game-state) (.-mousedown gs/game-state))
      (.beginPath ctx)
      (.moveTo ctx (.-strokestartX gs/game-state) (.-strokestartY gs/game-state))
      (.lineTo ctx (.-mouseX gs/game-state) (.-mouseY gs/game-state))
      (.stroke ctx))
    ;draw strokes. TODO: optimize for onscreen lol
    (doseq [stroke (.-strokes gs/game-state)]
      (let [sx (world->screen :x (.-sx stroke))
            sy (world->screen :y (.-sy stroke))
            ex (world->screen :x (.-ex stroke))
            ey (world->screen :y (.-ey stroke))]
        (.beginPath ctx)
        (.moveTo ctx sx sy) 
        (.lineTo ctx ex ey) 
        (.stroke ctx)))
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
                  (recur))))))))))


#_(defn weighted-random-choice [weights]
   (let [entries (js/Array.from (.entries weights))
         total (reduce (fn [acc [k v]] (+ acc v)) 0 entries)]
     (when (pos? total)
       (let [r (* (.random js/Math) total)]
         (loop [cumulative 0
                remaining entries]
           (if-let [[k v] (first remaining)]
             (if (>= (+ cumulative v) r)
               k
               (recur (+ cumulative v) (rest remaining)))
             ;; This should never be reached with valid input
             nil))))))

#_(defn markov-chain [seed n]
    (loop [output [seed]
           current seed]
      (if (< (count output) n)
        (let [nextword (weighted-random-transition 
                         (.get (.-transitiontable gs/game-state) current))]
          (recur (conj nextword output)
                 nextword))
        (.join " " output))))

(defn load-kjv! []
  (let [lines (.split kjv/kjv "\n")
        choice (rand-nth lines)]
    (markovfy-kjv!)
    (set! (.-lines gs/game-state) lines)
    (.push (.-texts gs/game-state) (texts/create-text-node choice 0 0))))

(defn begin-stroke! [e]
  (when (.-activeword gs/game-state)
      (set! (.-strokestartX gs/game-state) (.-clientX e))
      (set! (.-strokestartY gs/game-state) (.-clientY e))))

(defn end-stroke! [e]
  (let [sxpos (screen->world :x (.-strokestartX gs/game-state))
        sypos (screen->world :y (.-strokestartY gs/game-state))
        expos (screen->world :x (.-clientX e))
        eypos (screen->world :y (.-clientY e))]
    (when (.-activeword gs/game-state)
      (println (.get (.-transitiontable gs/game-state) (.-activeword gs/game-state)))

      (println (weighted-random-choice 
                 (.get (.-transitiontable gs/game-state) (.-activeword gs/game-state))))
      (.push (.-strokes gs/game-state) {:sx sxpos :sy sypos :ex expos :ey eypos}))))

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

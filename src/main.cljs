(ns main
  (:require 
    [texts :as texts]
    [gamestate :as gs]))


(defn create-canvas! [id]
  (let [id-string (str "#canvas_" id)
        try-canv (js/document.querySelector id-string)
        canv (if try-canv
               try-canv
               (js/document.createElement "canvas"))
        w (.-innerWidth js/window)
        h (.-innerHeight js/window)
        ratio (.-devicePixelRatio js/window)] 
    (println w)
    (println h)
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
    

(defn point-in-rect? [px py x y w h]
  (and (> px x)
       (> py y)
       (< px (+ x w))
       (< py (+ y h))))

(defn update-camera-pos! []
  (let [targX (:xpos (nth (:texts gs/game-state) (:targetnode gs/game-state)))
        targY (:ypos (nth (:texts gs/game-state) (:targetnode gs/game-state)))
        cX (:cameraX gs/game-state)
        cY (:cameraY gs/game-state)
        dX (- targX cX) 
        dY (- targY cY) 
        dist (.sqrt js/Math (+ (* dX dX) (* dY dY))) ;Distance to target
        dt (:dt gs/game-state)  ;Delta time (time since last frame)
        smoothing 5.0        ;Controls deceleration speed
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

(defn draw! [ctx canvas]
  (ctx.clearRect 0 0 (.-width canvas) (.-height canvas))
  (texts/set-text-info!)

  (.fillText ctx (:activeword gs/game-state) 50 50)
  (.fillText ctx (:targetnode gs/game-state) 50 70)
  (.fillText ctx (/ (:dt gs/game-state)) 50 30 70)
  (doseq [[idx element] (map-indexed vector (:texts gs/game-state))
          word    (.-words (.-drawdata element))]
    (let [wordx (.-xpos word)
          wordy (.-ypos word)
          cx (.-cameraX gs/game-state)
          cy (.-cameraY gs/game-state)
          xpos (+ (- wordx cx) (/ (.-innerWidth js/window) 2) (.-xpos element))
          ypos (+ (- wordy cy) (/ (.-innerHeight js/window) 2) (.-ypos element))]
      (.fillText ctx (:word word) xpos ypos)))) 

(defn main [ftime] ;frame time
  (let [ctx (:context gs/game-state)
        canvas (:canvas gs/game-state)]
    (set! gs/game-state.dt (/ (- ftime (:lastUpdate gs/game-state)) 1000))
    (set! gs/game-state.lastUpdate ftime)
    (update-camera-pos!) 
    (draw! ctx canvas)
    (js/window.requestAnimationFrame main)))

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
    (.addEventListener canvas "mouseleave" (fn [_] (set! gs/game-state.mousedown false)))
    (.addEventListener canvas "mouseup" (fn [_] (set! gs/game-state.mousedown false)))
    (.addEventListener canvas "mousedown" (fn [_] (set! gs/game-state.mousedown true)))
    (.addEventListener canvas "mousemove" (fn [{:keys [offsetX offsetY]}]
                                            (set! gs/game-state.mouseX offsetX)
                                            (set! gs/game-state.mouseY offsetY)))
    (.push (:texts gs/game-state) (texts/create-text-node "hello my name is patrick. I hope this text is long enough to showcase the splitting!" 0 0) (texts/create-text-node "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce vehicula pharetra tristique. Praesent sed lectus tristique, vestibulum risus vel, condimentum sem. Morbi interdum, nisl id finibus commodo, felis tellus pulvinar velit, vitae convallis orci purus posuere urna. Cras venenatis lorem a vestibulum pulvinar. Proin pharetra aliquam metus, eget faucibus neque. Donec tincidunt lobortis nisi vitae fermentum. Aliquam iaculis mi scelerisque libero efficitur vestibulum. In hac habitasse platea dictumst. Proin efficitur, orci sed pretium sodales, odio ligula tempus libero, ut dapibus sapien nibh sit amet odio. Sed ac neque nec est auctor egestas sollicitudin ullamcorper velit. Duis malesuada ex dui, a porttitor est tincidunt at." 100 150))
    (js/window.requestAnimationFrame main)))
    

(init)

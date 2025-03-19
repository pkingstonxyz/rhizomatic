(ns utils
  (:require [gamestate :as gs]))

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

(defn distance-between [sx sy ex ey]
  (.sqrt js/Math (+ (* (- ex sx) (- ex sx))
                    (* (- ey sy) (- ey sy)))))

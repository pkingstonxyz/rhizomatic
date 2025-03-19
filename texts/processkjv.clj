(def text (slurp "kjv.txt"))
(require '[clojure.string :as string])

(->> (string/split-lines text)
     (drop 1)
     (take 5)
     (map-indexed (fn [idx line]
                    (let [grouping (string/join " " (take 2 (string/split line #"\s")))
                          line     (string/join " " (drop 2 (string/split line #"\s")))]
                      {:group grouping
                       :line line
                       :idx idx})))
     (group-by :group)
     (sort-by :idx)
     (take 5)
     (println))

(ns ipjs.core
  (:refer-clojure :exclude [cat resolve]))

(enable-console-print!)
(defn on-js-reload [])

(def log #(.log js/console %))

(set! (.-ipfs js/window) (.ipfsAPI js/window))

(declare construct)

(def cache (set! (.-cache js/window) #js {}))
(defn dag? [o] (= (aget o "Data") "\b"))
(defn link? [s] (re-find #"^Qm[a-zA-Z0-9]{44}$" s))

(defn- ipfs-fn 
  ([prop] (ipfs-fn js/ipfs prop))
  ([root prop]
    (fn [data f]
      ((aget root prop) data
        (fn [err res]
          (if (or err (not res))
              (.error js/console err)
              ((or f identity) res)))))))

(def add (ipfs-fn "add"))
(def cat (ipfs-fn "cat"))
(def object-stat (ipfs-fn (.-object js/ipfs) "stat"))
(def object-get (ipfs-fn (.-object js/ipfs) "get"))

(defn subname [s] (last (re-find #"(.*\\)?([^\\]*)$" s)))

(defn js-eval [b] (js/eval (str "(" (.toString b) ")")))

(defn resolve 
  ([s] (or (aget cache s) (resolve s identity)))
  ([s cb]
    (if-let [cached (aget cache s)] 
      (do (cb cached) cached)
      (let [root #js {}]
        (aset cache s root)
        (object-get s 
          (fn [v]
            (if (dag? v) 
              (construct v cb root) 
              (if-let [cached (get cache s)] 
                (cb cached)
                (cat s #(cb (aset cache s (js-eval %)))))))) 
        root))))

(defn construct 
  ([o cb] (construct o cb #js {}))
  ([o cb root]
    (.map (.-Links o)
      (fn [link] 
        (resolve (.-Hash link) 
          #(aset root (subname (.-Name link)) %)))) 
    (cb root)))

(set! (.-universe js/window) 
  (js/Proxy. #js {} 
    #js {:get 
      (fn [target name receiver] 
        (or (aget cache name) 
            (if (link? name) 
                (resolve name 
                  #(.info js/console name))
                (aget target name))))}))

(set! (.-resolve js/window) resolve)
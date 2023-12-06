(ns testing-gui.core
  (:require
   [etaoin.api :as e]
   [etaoin.keys :as k]
   [clojure.string :as str]))



(comment (defonce driver (e/firefox)))
(def ^:dynamic *root* (or (System/getenv "URL")
                        "https://practicesoftwaretesting.com/#/"))


(def admin {:password "welcome01"
            :email "admin@practicesoftwaretesting.com"
            :name "John Doe"})

(defn by-text ([class text] {:xpath (format "//%s[contains(text(),'%s')]" class text)})
  ([text] (by-text "*" text)))

(defn nav [x] (format "//*[@data-test='%s']" x))

(defn login [driver acc]
  (e/go driver *root*)
  (if (e/has-text? driver (:name admin)) true
      (do (doto driver
            (e/wait-visible (by-text "Sign in"))
            (e/click (by-text "Sign in"))
            (e/wait-visible "//input[@class='btnSubmit']")
            (e/fill "//form//input[@id='password']" (:password acc))
            (e/fill "//form//input[@id='email']" (:email acc))
            (e/wait 0.5)
            (e/click "//input[@class='btnSubmit']")
            (e/wait-visible "//li/a")
            (e/wait 1))
          (e/has-text? driver (:name admin)))))
(defn logout [driver]
  (doto driver
    (e/go *root*)
    (e/wait-visible "//li/a")
    (e/wait 0.1)
    (e/click (nav "nav-user-menu"))
    (e/wait-has-text-everywhere "Sign out")
    (e/wait 0.1)
    (e/click (nav "nav-sign-out"))))

;; (logout driver)
;; (login driver admin)

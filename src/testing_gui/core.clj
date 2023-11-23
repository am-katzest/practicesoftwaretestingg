(ns testing-gui.core
  (:require
   [etaoin.api :as e]
   [etaoin.keys :as k]
   [clojure.string :as str]))

(defonce driver (e/firefox))


(def root "https://practicesoftwaretesting.com/#/")
(def admin {:password "welcome01"
            :email "admin@practicesoftwaretesting.com"
            :name "John Doe"})

(defn by-text ([class text] {:xpath (format "//%s[contains(text(),'%s')]" class text)})
  ([text] (by-text "*" text)))

(defn login [driver acc]
  (e/go driver root)
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
    (e/go root)
    (e/wait-visible "//li/a")
    (e/click "//nav[1]//div[1]//div[1]//ul[1]//li[4]//a[1]")
    (e/wait-has-text-everywhere "Sign out")
    (e/click "/html[1]/body[1]/app-root[1]/app-header[1]/nav[1]/div[1]/div[1]/ul[1]/li[4]/ul[1]/li[13]/a[1]")
    ))
;; (logout driver)
;; (login driver admin)

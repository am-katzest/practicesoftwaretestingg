(ns testing-gui.core-test
  (:require [clojure.test :refer :all]
            [testing-gui.core :refer :all]
            [etaoin.api :as e]
            [etaoin.keys :as k]
            [clojure.string :as str]))


(defn goto-category-panel [driver]
  (is (= true (login driver admin)) "couldn't log in")
  (doto driver
    (e/go (str url "/admin/categories"))
    (e/wait-has-text-everywhere "Category")
    (e/wait 1)))


(defn add-category [driver cath]
  (doto driver
    (goto-category-panel)
    (e/wait-has-text-everywhere "Add Category")
    (e/click (by-text "Add Category"))
    (e/wait-has-text-everywhere "Save")
    (e/fill-multi cath)
    (e/click :parent_id)
    (e/wait-has-text-everywhere (:parent_id cath))
    (e/click (by-text (:parent_id cath)))
    (e/wait 1)
    (e/click (by-text "Save"))
    (e/wait-visible {:tag :div :fn/has-class "alert"})
    (e/wait 1)))



(deftest add-kat-pos
  (e/with-firefox driver
    (let [uuid (random-uuid)
          name (str "koty domowe" uuid)
          cat {:parent_id "Other"
               :name name
               :slug (str "koty-domowe" uuid)}]
      (doto driver
        (login admin)
        (goto-category-panel))
      (is (= false (e/has-text? driver name)) "kategoria już istnieje")
      (add-category driver cat)
      (is (= "Category saved!"
             (e/get-element-text driver {:tag :div :fn/has-class "alert"}))
          "nie udało się dodać kategorii")
      (goto-category-panel driver)
      (is (= true (e/has-text? driver name))
          "kategorii jakimś cudem nie ma na panelu"))))

(deftest add-kat-neg-duplicate
  ;; should run after add-kat-pos
  (e/with-firefox driver
    (doto driver
      (login admin)
      (goto-category-panel))
    (let [name (try (e/get-element-text driver (by-text "koty domowe"))
                    (catch Throwable _
                      (is false "no category koty-domowe yet")
                      "hand tools"))
          cat {:parent_id "Other"
               :name name
               :slug (str/replace name  " " "-")}]
      (is (= true (e/has-text? driver name)) "kategoria jeszcze nie istnieje")
      (add-category driver cat)
      (is (= "A category already exists with this slug."
             (e/get-element-text driver {:tag :div :fn/has-class "alert"}))
          "udało się dodać kategorię pomimo tego że jest duplikatem"))))

(deftest add-kat-neg-whitespace
  (e/with-firefox driver
    (let [uuid (random-uuid)
          name (str "koty domowe" uuid)
          cat {:parent_id "Other"
               :name name
               :slug name}]
      (doto driver
        (login admin)
        (goto-category-panel))
      (is (= false (e/has-text? driver name)) "kategoria już istnieje")
      (add-category driver cat)
      (is (= "Slug cannot contain spaces"
             (e/get-element-text driver {:tag :div :fn/has-class "alert"}))
          "zły kod błędu")
      (goto-category-panel driver)
      (is (= false (e/has-text? driver name))
          "błędna kategoria jest widoczna w panelu"))))

(defmacro ex->nil [& forms]
  `(try ~@forms
     (catch Throwable ~'_ nil)))

(deftest mod-kat-pos
  ;; should run after add-kat-pos
  (e/with-firefox driver
    (doto driver
      (login admin)
      (goto-category-panel))
    (let [slug (ex->nil (e/get-element-text driver (by-text "koty-domowe")))
          newname (str "sierściuchy" (random-uuid))]
      (is (some? slug) "brak odpowiedniej kategorii")
      (doto driver
        (e/click  (format "(//td[contains(text(), '%s')]/parent::*)//a[contains(text(), 'Edit')]" slug))
        (e/wait-visible :name)
        (e/clear :name)
        (e/fill :name newname)
        (e/wait 1)
        (e/click (by-text "Save"))
        (e/wait-visible {:tag :div :fn/has-class "alert"})
        (e/wait 1))
      (is (= "Category saved!"
             (e/get-element-text driver {:tag :div :fn/has-class "alert"}))
          "nie udało się dodać kategorii")
      (goto-category-panel driver)
      (is (= true (e/has-text? driver newname))
          "kategorii jakimś cudem nie ma na panelu"))))



(deftest mod-kat-neg
  ;; should run after add-kat-pos
  (e/with-firefox driver
    (doto driver
      (login admin)
      (goto-category-panel))
    (let [slug (ex->nil (e/get-element-text driver (by-text "sierściuchy")))
          newname (str "meow  meow" (random-uuid))]
      (is (some? slug) "brak odpowiedniej kategorii")
      (doto driver
        (e/click  (format "(//td[contains(text(), '%s')]/parent::*)//a[contains(text(), 'Edit')]" slug))
        (e/wait-visible :slug)
        (e/clear :slug)
        (e/fill :slug newname)
        (e/wait 1)
        (e/click (by-text "Save"))
        (e/wait-visible {:tag :div :fn/has-class "alert"})
        (e/wait 1))
      (is (= "Slug cannot contain spaces"
             (e/get-element-text driver {:tag :div :fn/has-class "alert"}))
          "zła wiadomość błędu")
      (goto-category-panel driver)
      (is (= false (e/has-text? driver newname))
          "zła kategoria została dodana"))))

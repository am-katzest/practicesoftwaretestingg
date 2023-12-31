(ns testing-gui.core-test
  (:require [clojure.test :refer :all]
            [testing-gui.core :refer :all]
            [etaoin.api :as e]
            [etaoin.keys :as k]
            [new-ways.clj-cukes :refer [Given run-gherkin-file When Then And]]
            [clojure.string :as str]))


(defmacro ex->nil [& forms]
  `(try ~@forms
     (catch Throwable ~'_ nil)))

(defn goto-category-panel [driver]
  (doto driver
    (e/go (str *root* "/admin/categories"))
    (e/wait-has-text-everywhere "Category")
    (e/wait 1)))

(defn just-add-category
  "without simulating full user interaction"
  [driver cath]
  (doto driver (e/go (str *root* "/admin/categories/add"))
        (e/wait-has-text-everywhere "Add Category")
        (e/wait 0.2)
        (e/fill-multi (dissoc cath :parent_id))
        (e/wait 0.2)
        (e/select :parent_id (:parent_id cath))
        (e/wait 0.5)
        (e/click (by-text "Save"))))
;; (just-add-category driver {:parent_id "Other"
;;                     :name "meow"
;;                     :slug (str "koty-domowe31713137")})
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
    (e/wait 0.5)))

(defn delete-category [driver slug]
  (doto driver
    (goto-category-panel))
  (while (e/has-text? driver slug)
    (doto driver
      (e/wait 1)
      (e/double-click (format "(//td[contains(text(), '%s')]/parent::*)//button" slug))
      (goto-category-panel))))

(defn just-type [text]
  {:type    "key"
   :id (random-uuid)
   :actions (->> text
                 (map str)
                 (mapcat (fn [key]
                           [{:type "keyDown" :value key}
                            {:type "keyUp" :value key}])))})

(defn register [driver form]
  (doto driver
    (e/go *root*)
    (e/wait-has-text-everywhere "Sign in")
    (e/wait 0.1)
    (e/click (by-text "Sign in"))
    (e/wait-has-text-everywhere "Register")
    (e/wait 0.1)
    (e/click (by-text "Register"))
    (e/wait-has-text-everywhere "Register")
    (e/wait 0.1)
    (e/fill-multi (dissoc form :date :country)) ;these two are tricky
    (e/select :country (:country form))
    (e/click :dob)
    (e/wait 0.1)
    (e/perform-actions (just-type (:date form)))
    (e/wait 0.1)
    (e/click (by-text "Register"))))

(def base-form
  {:first_name "kitty"
   :last_name "cat"
   :postcode "123-35"
   :city "mrau"
   :address "miaaaau 13"
   :phone "123456789"
   :email "kibby@mraumail.com"
   :state "somestate"
   :password "faggot :3333"
   :country "Poland"
   :date "03052003"})

(deftest testing-account-creation
  (e/with-firefox driver
    (testing "creating-account-positive"
      (let [r (->> (random-uuid) str (take 5) (reduce str))
            email (str r "@mraumail.com")
            pass (random-uuid)
            form (assoc base-form :email email :password pass)]
        (doto driver
          (register form)
          (e/wait 0.5)
          (e/wait-has-text-everywhere "Login"))
        (is (= (str *root* "auth/login") (e/get-url driver)) "didn't get redirected to registration")
        (doto driver
          (e/fill-multi {:password pass :email email})
          (e/wait 0.5)
          (e/click "//input[@value='Login']")
          (e/wait 1)
          (e/wait-has-text-everywhere "My account"))
        (is (= true (e/has-text? driver (str (:first_name form) " " (:last_name form)))) "account creation failed")))
    (logout driver)
    (testing "creating-account-negative"
      (let [r (->> (random-uuid) str (take 5) (reduce str))
            email (str r " wrong mail")
            pass (random-uuid)
            form (assoc base-form :email email :password pass)]
        (doto driver
          (register form)
          (e/wait-visible {:tag :div :fn/has-class "alert"}))
        (is (= true (e/has-text? driver "E-mail format is invalid")) "brak powiadomienia o niepoprawnym mailu")))))

(def logins {"admin" admin})

(Given #"I'm logged in as (\w+)" [driver user]
       (is (= true (login driver (logins user))))
       driver)

(When #"I attempt to add a category with name \"([^\"]+)\", slug \"([^\"]+)\""
      [driver name slug]
      (add-category driver {:name name :slug slug :parent_id "Other"})
      driver)

(And #"there exists a category with name \"([^\"]+)\", slug \"([^\"]+)\""
      [driver name slug]
      (just-add-category driver {:name name :slug slug :parent_id "Other"})
      driver)

(Then #"the box should appear saying \"([^\"]+)\"" [driver box-text]
      (is (= true (e/exists? driver {:tag :div :fn/has-class "alert"})) "no box appeared")
      (is (= box-text
             (e/get-element-text driver {:tag :div :fn/has-class "alert"}))
          "wrong popup text")
      driver)

(And #"on the list there should be a category named \"([^\"]+)\"" [driver name]
     (goto-category-panel driver)
     (is (= true (e/has-text? driver name)) "no such category")
     driver)

(And #"on the list there shouldn't be a category named \"([^\"]+)\"" [driver name]
     (goto-category-panel driver)
     (is (= false (e/has-text? driver name)) "category is on the page but it shouldn't")
     driver)

(When #"I attempt to modify category \"([^\"]+)\"" [driver slug]
      (doto driver
        (goto-category-panel)
        (e/click  (format "(//td[contains(text(), '%s')]/parent::*)//a[contains(text(), 'Edit')]" slug))
        (e/wait-visible :name)
        (e/wait 0.1)))

(And #"set name to \"([^\"]+)\"" [driver new-name]
     (doto driver
       (e/clear :name)
       (e/fill :name new-name)
       (e/wait 0.5)))

(And #"set slug to \"([^\"]+)\"" [driver new-slug]
     (doto driver
       (e/clear :slug)
       (e/fill :slug new-slug)
       (e/wait 0.5)))

(And #"save" [driver]
     (doto driver
       (e/click (by-text "Save"))
       (e/wait-visible {:tag :div :fn/has-class "alert"})
       (e/wait 0.5)))


(deftest category-testing-cucumber
  (e/with-firefox driver
    (try (run-gherkin-file driver "test/features/categories_manipulation.feature")
         (finally
           (delete-category driver "koty-domowe")))))

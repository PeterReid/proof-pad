(in-package "ACL2")

:set-state-ok t

(defconst *default-repeat* 50)

(defun random-between-fn (low high state)
  (mv-let (random state)
    (random$ (- high low) state)
    (mv (+ random low) state)))

(defmacro random-between (low high)
  `(random-between-fn ,low ,high state))

(defabbrev random-natural ()
  (random-between 0 1000))

(defabbrev random-integer ()
  (random-between -1000 1000))

(defmacro repeat-times (times limit body)
  (if (zp limit)
    `(mv state (hard-error nil
                           "Wasn't able to generate enough data."
                           nil))
    `(if (zp ,times)
         (mv state nil)
         (mv-let (state result assignments)
            ,body
            (mv-let (state rs)
                 (repeat-times (- ,times 
                                  (if (eql result
                                           'where-not-matched) 0 1))
                               ,(- limit 1) ,body)
                 (mv state
                     (cons (cons result assignments)
                           rs)))))))

(defmacro expand-vars (vars body)
  (if (endp vars)
    `(mv state ,body nil)
    `(mv-let (,(first vars) state)
       ,(cond ((eql (second vars) ':value)
               (third vars))
              ((eql (fourth vars) ':value)
               (fifth vars))
              (t (hard-error
                   nil
                   "Missing :value parameter for ~xn"
                   (list (cons #\n (first vars))))))
       (if ,(cond ((eql (second vars) ':where)
                   (third vars))
                  ((eql (fourth vars) ':where)
                   (fifth vars))
                  (t t))
         (mv-let (state result assignments)
           (expand-vars ,(cond ((member (fourth vars)
                                        '(:value :where))
                                (nthcdr 5 vars))
                               (t (nthcdr 3 vars)))
                        ,body)
           (mv state result
               (cons (cons (quote ,(first vars))
                           ,(first vars))
                     assignments)))
         (mv state 'where-not-matched nil)))))

(defun eager-and (x y)
  (and x y))

(defun condense-results (rs)
  (if (endp rs)
    t
    (eager-and (let ((success (first (first rs))))
           (prog2$
             (if (not success)
               (cw "Failed with assignments:    ~&0~%" (rest (first rs)))
               (cw "Succeeded with assignments: ~&0~%" (rest (first rs))))
             success))
         (condense-results (rest rs)))))

(defmacro defproperty (name &rest args)
  (let ((repeat (cond ((eql (first args) ':repeat)
                       (second args))
                      ((eql (third args) ':repeat)
                       (fourth args))
                      (t *default-repeat*)))
        (limit (cond ((eql (first args) ':limit)
                      (second args))
                     ((eql (third args) ':limit)
                      (fourth args))
                     (t 60)))
        (vars (cond ((<= (len args) 3) (first args))
                    ((<= (len args) 5) (third args))
                    ((<= (len args) 7) (fifth args))))
        (body (cond ((<= (len args) 3) (second args))
                    ((<= (len args) 5) (fourth args))
                    ((<= (len args) 7) (sixth args)))))
    `(mv-let (state results)
       (repeat-times ,repeat ,limit
                     (expand-vars ,vars ,body))
       (if (condense-results results)
         (mv nil state)
         (mv (hard-error nil "Test ~xn failed."
                         (list (cons #\n (quote ,name))))
             state)))))

:trans1 (repeat-times 20 20 x)

(defproperty +-commutes
  (x :value (random-natural)
   y :value (random-natural))
  (= (+ x y)
     (+ y x)))

(defproperty --commutes :repeat 10 :limit 100
  (x :value (random-natural)
   y :value (random-natural) :where (= x y))
  (= (- x y)
     (- y x)))

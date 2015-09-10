cd /home/carlos/Komunike/login
grep -Rl $1 src/clj src/cljs src/cljc | sed 's/src\/clj*.\///g' | sed 's/\.clj*.//g' | sed 's/\//\./g' | sed 's/\_/\-/g'

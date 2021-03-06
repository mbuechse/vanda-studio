# Tiburon Intersection
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Intersection
# IN wrtg1 :: RegularTreeGrammar
# IN wrtg2 :: RegularTreeGrammar
# OUT wrtg :: RegularTreeGrammar
#
# Intersects two weighted regular tree grammars.
TiburonIntersect () {
	java -jar -Duser.language=en -Duser.country=US "$TIBURON/tiburon.jar" "$2" "$3" > "$4"
}

# Tiburon n-best
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models
# IN wrtg :: RegularTreeGrammar
# IN n :: Integer
# OUT trees :: TiburonTreeCorpus
# OUT scores :: Scores
#
# Calculates n-best trees and their weights.
TiburonNBest () {
	java -jar -Duser.language=en -Duser.country=US "$TIBURON/tiburon.jar" "$2" "-k$3" > "$1/tmp.txt"
	cat "$1/tmp.txt" | sed 's/#.*//' > "$4"
	cat "$1/tmp.txt" | sed 's/^.*# //' > "$5"
	echo "$4" > "$5.meta"
}

# Tiburon n-best yield
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models
# IN wrtg :: RegularTreeGrammar
# IN n :: Integer
# OUT sentences :: SentenceCorpus
# OUT scores :: Scores
#
# Calculates n-best yields and their weights.
TiburonNBestY () {
	java -jar -Duser.language=en -Duser.country=US "$TIBURON/tiburon.jar" "$2" "-k$3" -y > "$1/tmp.txt"
	cat "$1/tmp.txt" | sed 's/#.*//' > "$4"
	cat "$1/tmp.txt" | sed 's/^.*# //' > "$5"
	echo "$4" > "$5.meta"
}

# Tiburon apply tree transducer
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models
# IN transducer :: TreeTransducer
# IN treesIn :: TiburonTreeCorpus
# OUT treesOut :: TiburonTreeCorpus
# OUT scores :: Scores
#
# Applies a tree transducer.
TiburonApplyTT () {
	java -jar -Duser.language=en -Duser.country=US "$TIBURON/tiburon.jar" "$2" "$3" "-k1" > "$1/tmp.txt"
	cat "$1/tmp.txt" | sed 's/#.*//' > "$4"
	cat "$1/tmp.txt" | sed 's/^.*# //' > "$5"
	echo "$4" > "$5.meta"
}
source "$FUNCDIR/util.bash"

# IRSTLM
# Version: 2012-12-03
# Contact: Tobias.Denkinger@mailbox.tu-dresden.de
# Category: language model
# IN English Corpus :: Sentence Corpus
# IN n-gram length :: Integer
# OUT ARPA n-grams :: ARPA
#
# Computes n-gram probabilities.
IRSTLM () {
	echo "Running: IRSTLM..."
	TMP="$OUTPATH/IRSTLM_TMP"
	mkdir -p "$TMP"
	pathAndName "$1" f1 i1
	out="$OUTPATH/IRSTLM($i1,$2).0"
	"$IRSTLM/add-start-end.sh" < "$f1" > "$TMP/train.txt"
	"$IRSTLM/ngt" -i="$TMP/train.txt" -n="$2" -o="$TMP/train.www"
	"$IRSTLM/tlm" -tr="$TMP/train.www" -n="$2" -lm=wb -o="$out"
	rm -rf "$TMP"
	eval $3=\"$out\"
	echo "Done."
}

# ARPA2Binary
# Version: 2012-12-04
# Contact: Tobias Denkinger@mailbox.tu-dresden.de
# Category: language model
# IN ARPA n-grams :: ARPA
# OUT binary n-grams :: Binary n-grams
#
# Converts iARPA to binary n-gram format.
ARPA2Binary () {
	echo "Running: ARPA2Binary..."
	pathAndName "$1" f1 i1
	out="$OUTPATH/ARPA2Binary($i1).0"
	"$IRSTLM/compile-lm" "$f1" "$out"
	echo "Done."
}
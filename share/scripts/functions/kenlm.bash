source "$FUNCDIR/util.bash"

# KenLM
# Version: 2013-01-31
# Contact: Tobias.Denkinger@mailbox.tu-dresden.de
# Category: language model
# IN n-gram model :: ARPA
# IN English corpus :: SentenceCorpus
# OUT logarithmic scores :: Scores
#
# Evaluates the corpus according to the given model.
KenLM () {
	echo "Running: KenLM..."
	export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
	"runhaskell" "$VANDADIR/programs/NGrams_KenLM.hs" -g "$1" -i "$2" -o "$3"
	echo "Done."
}


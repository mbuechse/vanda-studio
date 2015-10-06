# XRSTranslate
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: translation
# IN rules :: GHKMHypergraph
# IN sentence corpus :: SentenceCorpus
# OUT tree corpus :: PennTreeCorpus
#
# Generates a Tree Corpus given a GHKM Hypergraph and a Sentence Corpus
XRSTranslate () {
	"$VANDA/programs/XRSToHypergraph" t2b -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" < "$2"
	"$VANDA/programs/XRSTranslate" -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" --complicated < "$3" > "$4"
}

# PennToSentenceCorpus
# Version: 2013-10-10
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Corpus Tools
# IN tree corpus :: PennTreeCorpus
# OUT sentence corpus :: SentenceCorpus
#
# Reads of the yield of trees in a PennTreeCorpus.
PennToSentenceCorpus () {
	"$VANDA/programs/PennToSentenceCorpus" < "$2" > "$3"
}

# XRSNGrams
# Version: 2014-10-07
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: intersection
# IN rulesIn :: GHKMHypergraph
# IN ngrams :: ARPA
# OUT rulesOut :: GHKMHypergraph
#
# Intersects a language model in ARPA format with a GHKM hypergraph.
XRSNGrams () {
	"$VANDA/programs/XRSToHypergraph" t2b -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" < "$2"
	"$VANDA/dist/build/Vanda/Vanda" xrsngrams product --no-backoff "$1/zhg" "$1/map.e" "$1/map.f" "$3"
	"$VANDA/programs/XRSToHypergraph" b2t -e "$1/map.e" -f "$1/map.f" -z "$1/zhg.new" > "$4"
}

# XRSNGramsTranslate
# Version: 2014-10-07
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: translation
# IN rules :: GHKMHypergraph
# IN ngrams :: ARPA
# IN beam :: Integer
# IN corpus :: SentenceCorpus
# OUT translation :: SentenceCorpus
#
# Translates a SentenceCorpus using a GHKM hypergraph and a language model.
XRSNGramsTranslate () {
	"$VANDA/programs/XRSToHypergraph" t2b -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" < "$2"
	if [ "$4" -eq "0" ]; then
		"$VANDA/dist/build/Vanda/Vanda" xrsngrams translate --no-backoff "$1/zhg" "$1/map.e" "$1/map.f" "$3" < "$5" > "$6"
	else
		"$VANDA/dist/build/Vanda/Vanda" xrsngrams translate --prune="$4" "$1/zhg" "$1/map.e" "$1/map.f" "$3" < "$5" > "$6"
	fi
}

# NGrams
# Version: 2014-10-07
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: language model
# IN model :: ARPA
# IN corpus :: SentenceCorpus
# OUT scores :: Scores
#
# Evaluates the corpus according to the given model.
NGrams () {
	"$VANDA/dist/build/Vanda/Vanda" ngrams evaluate "$2" < "$3" > "$4"
	echo "$3" > "${4}.meta"
}

# NGramsTrain
# Version: 2014-10-07
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: language model
# IN degree :: Integer
# IN minReliableCount :: Integer
# IN corpus :: SentenceCorpus
# OUT model :: ARPA
#
# Trains an n-gram model.
NGramsTrain () {
	"$VANDA/dist/build/Vanda/Vanda" ngrams train --bound="$3" --degree="$2" < "$4" > "$5"
}

# ExtragtPLCFRS
# Version: 2015-07-09
# Contact: sebastian.mielke@tu-dresden.de
# Category: Language Model
# IN corpus :: NeGraCorpus
# OUT plcfrs :: PLCFRS
#
# Extract a probabilistic LCFRS from a corpus (NEGRA export format)
ExtractPLCFRS () {
  iconv "--from-code=$(file --brief --mime-encoding "$2")" "--to-code=utf-8" "$2" | "$VANDA/dist/build/Vanda/Vanda" lcfrs extract "$3"
}

# BinarizeLCFRSNaively
# Version: 2015-07-09
# Contact: sebastian.mielke@tu-dresden.de
# Category: Language Model
# IN plcfrs :: PLCFRS
# OUT binarizedplcfrs :: PLCFRS
#
# Binarizes a probabilistic LCFRS naively
BinarizeLCFRSNaively () {
	"$VANDA/dist/build/Vanda/Vanda" lcfrs binarize --naive "$2" "$3"
}

# BinarizeLCFRSLowMaxFo
# Version: 2015-07-09
# Contact: sebastian.mielke@tu-dresden.de
# Category: Language Model
# IN plcfrs :: PLCFRS
# OUT binarizedplcfrs :: PLCFRS
#
# Binarizes a probabilistic LCFRS optimally (lowest maximal fanout)
BinarizeLCFRSLowMaxFo () {
	"$VANDA/dist/build/Vanda/Vanda" lcfrs binarize --optimal "$2" "$3"
}

# BinarizeLCFRSHybrid
# Version: 2015-07-31
# Contact: sebastian.mielke@tu-dresden.de
# Category: Language Model
# IN plcfrs :: PLCFRS
# OUT binarizedplcfrs :: PLCFRS
#
# Binarize rules up to rank 5 optimally and the rest naively.
BinarizeLCFRSHybrid () {
	"$VANDA/dist/build/Vanda/Vanda" lcfrs binarize --hybrid=5 "$2" "$3"
}

# BinarizeLCFRSHybrid
# Version: 2015-07-31
# Contact: sebastian.mielke@tu-dresden.de
# Category: Language Model
# IN bound :: Integer
# IN plcfrs :: PLCFRS
# OUT binarizedplcfrs :: PLCFRS
#
# Binarize rules up to rank "bound" optimally and the rest naively.
BinarizeLCFRSHybrid2 () {
	"$VANDA/dist/build/Vanda/Vanda" lcfrs binarize --hybrid="$2" "$3" "$4"
}

# Vanda-pcfg-extract
# Version: 2015-10-06
# Contact: Toni.Dietze@tu-dresden.de
# Category: Language Models::Training
# IN trees :: PennTreeCorpus
# OUT pcfg :: VandaBinaryPCFG
#
# Extract a pcfg from treebank.
Vanda-pcfg-extract () {
	"${VANDA}/dist/build/Vanda/Vanda" pcfg extract --bout "$3" "$2"
}

# Vanda-pcfg-train
# Version: 2015-10-06
# Contact: Toni.Dietze@tu-dresden.de
# Category: Language Models::Training
# IN pcfg-in :: VandaBinaryPCFG
# IN sentences :: SentenceCorpus
# IN em-iterations :: Integer
# OUT pcfg-out :: VandaBinaryPCFG
#
# Estimate the rule probabilities of a pcfg with unsupervised training.
Vanda-pcfg-train () {
	"${VANDA}/dist/build/Vanda/Vanda" pcfg train --bin --bout "$2" "$5" "$3" "$4"
}

# Vanda-pcfg-n-best
# Version: 2015-10-06
# Contact: Toni.Dietze@tu-dresden.de
# Category: Language Models::Generation
# IN pcfg :: VandaBinaryPCFG
# IN count :: Integer
# OUT trees :: PennTreeCorpus
#
# Find the most probable parse trees of a pcfg.
Vanda-pcfg-bests () {
	"${VANDA}/dist/build/Vanda/Vanda" pcfg bests --bin "$2" "$3" > "$4"
}

# Vanda-pcfg-intersect
# Version: 2015-10-06
# Contact: Toni.Dietze@tu-dresden.de
# Category: Language Models::Parsing
# IN pcfg-in :: VandaBinaryPCFG
# IN sentence :: SingleSentence
# OUT pcfg-out :: VandaBinaryPCFG
#
# Intersect a pcfg with a sentence resulting in a pcfg that allows exactly those derivations that produce the given sentence.
Vanda-pcfg-intersect () {
	"${VANDA}/dist/build/Vanda/Vanda" pcfg intersect --bin --bout "$2" "$4" "$(cat "$3")"
}

# Vanda-pcfg-convert-bin-to-text
# Version: 2015-10-06
# Contact: Toni.Dietze@tu-dresden.de
# Category: Language Models::Conversion
# IN pcfg-in :: VandaBinaryPCFG
# OUT pcfg-out :: TextualBerkeleyGrammar
#
# Convert a binary representation of a pcfg into human readable text.
Vanda-pcfg-convert-bin-to-text () {
	"${VANDA}/dist/build/Vanda/Vanda" pcfg convert --bin "$2" "$3"
	mv "$3"{,.grammar}
}

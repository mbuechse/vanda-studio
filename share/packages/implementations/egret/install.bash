id="egret"
varname="EGRET"
version="2013-03-08"
binpath="$id"

download () {
	wget http://egret-parser.googlecode.com/files/Egret.zip
}

install_me () {
	unzip Egret.zip
	cd Egret
	sed -i "2 i #include <cstdlib>" Egret/src/Tree.cpp
	sed -i "3 i #include <cstdlib>" Egret/src/utils.h
	g++ Egret/src/*.cpp -O2 -o egret
	cp egret "$1"
	mkdir -p "$2/egret_grammars"
	mv -n -t "$2/egret_grammars" "eng_grammar" "chn_grammar"
	cd ..
}

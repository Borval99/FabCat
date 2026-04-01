module fabCat {
	requires transitive javafx.base;
	requires transitive javafx.graphics;
	requires transitive javafx.controls;
	requires transitive bluecove;
	exports fablab.fabcat; //altrimenti le classi non sono accessibili
}
import * as squint_core from 'squint-cljs/core.js';
var constants = ({ "MAXCAMERAMOVE": 100, "MAXTEXTWIDTH": 400 });
var game_state = ({ "mouseX": 0, "targetnode": 0, "cameraY": 0, "activeword": null, "mouseY": 0, "lastUpdate": 0, "texts": [], "dt": 0, "textwidth": 400, "transitiontable": new Map(), "strokes": [], "mousedown": false, "cameraX": 0 });

export { constants, game_state }

import * as squint_core from 'squint-cljs/core.js';
import * as texts from './texts.mjs';
import * as gs from './gamestate.mjs';
import * as kjv from './kjv.mjs';
var create_canvas_BANG_ = function (id) {
const id_string1 = squint_core.str("#canvas_", id);
const try_canv2 = document.querySelector(id_string1);
const canv3 = ((squint_core.truth_(try_canv2)) ? (try_canv2) : (document.createElement("canvas")));
const w4 = window.innerWidth;
const h5 = window.innerHeight;
const ratio6 = window.devicePixelRatio;
canv3.width = (w4) * (ratio6);
canv3.height = (h5) * (ratio6);
canv3.style.width = squint_core.str(w4, "px");
canv3.style.height = squint_core.str(h5, "px");
canv3.id = id_string1;
return canv3
};
var create_context_BANG_ = function (canvas) {
const context1 = canvas.getContext("2d");
context1.imageSmoothingEnabled = false;
return context1
};
var resize_canvas_BANG_ = function (canvas, context, ratio) {
const w1 = window.innerWidth;
const h2 = window.innerHeight;
canvas.width = (w1) * (ratio);
canvas.height = (h2) * (ratio);
canvas.style.width = squint_core.str(w1, "px");
canvas.style.height = squint_core.str(h2, "px");
gs.game_state.textwidth = Math.min(squint_core.get(gs.constants, "MAXTEXTWIDTH"), (w1) - (100));
context.scale(ratio, ratio);
return texts.remeasure_text_nodes_BANG_()
};
var point_in_rect_QMARK_ = function (px, py, x, y, w, h) {
const and__24283__auto__1 = (px) > (x);
if (and__24283__auto__1) {
const and__24283__auto__2 = (py) > (y);
if (and__24283__auto__2) {
const and__24283__auto__3 = (px) < ((x) + (w));
if (and__24283__auto__3) {
return (py) < ((y) + (h))} else {
return and__24283__auto__3}} else {
return and__24283__auto__2}} else {
return and__24283__auto__1}
};
var on_screen_QMARK_ = function (x, y, w, h) {
const bleft1 = x;
const btop2 = y;
const bright3 = (x) + (w);
const bbot4 = (y) + (h);
const and__24283__auto__5 = (bright3) > (0);
if (and__24283__auto__5) {
const and__24283__auto__6 = (bleft1) < (window.innerWidth);
if (and__24283__auto__6) {
const and__24283__auto__7 = (bbot4) > (0);
if (and__24283__auto__7) {
return (btop2) < (window.innerHeight)} else {
return and__24283__auto__7}} else {
return and__24283__auto__6}} else {
return and__24283__auto__5}
};
var world__GT_screen = function (x_or_y, coord) {
if ((x_or_y) === ("x")) {
return ((window.innerWidth) / (2)) + ((coord) - (gs.game_state.cameraX))} else {
return ((window.innerHeight) / (2)) + ((coord) - (gs.game_state.cameraY))}
};
var screen__GT_world = function (x_or_y, coord) {
if ((x_or_y) === ("x")) {
return (gs.game_state.cameraX) + ((coord) - ((window.innerWidth) / (2)))} else {
return (gs.game_state.cameraY) + ((coord) - ((window.innerHeight) / (2)))}
};
var update_camera_pos_BANG_ = function () {
const targX1 = squint_core.get(squint_core.nth(squint_core.get(gs.game_state, "texts"), squint_core.get(gs.game_state, "targetnode")), "xpos");
const targY2 = squint_core.get(squint_core.nth(squint_core.get(gs.game_state, "texts"), squint_core.get(gs.game_state, "targetnode")), "ypos");
const cX3 = squint_core.get(gs.game_state, "cameraX");
const cY4 = squint_core.get(gs.game_state, "cameraY");
const dX5 = (targX1) - (cX3);
const dY6 = (targY2) - (cY4);
const dist7 = Math.sqrt(((dX5) * (dX5)) + ((dY6) * (dY6)));
const dt8 = squint_core.get(gs.game_state, "dt");
const smoothing9 = 3;
const threshold10 = 0.5;
const factor11 = (1) - (Math.exp((-smoothing9) * (dt8)));
const new_cX12 = (cX3) + ((factor11) * (dX5));
const new_cY13 = (cY4) + ((factor11) * (dY6));
if ((dist7) < (threshold10)) {
gs.game_state.cameraX = targX1;
return gs.game_state.cameraY = targY2;
} else {
gs.game_state.cameraX = new_cX12;
return gs.game_state.cameraY = new_cY13;
}
};
var main = function (ftime) {
const ctx1 = squint_core.get(gs.game_state, "context");
const canvas2 = squint_core.get(gs.game_state, "canvas");
gs.game_state.dt = ((ftime) - (squint_core.get(gs.game_state, "lastUpdate"))) / (1000);
gs.game_state.lastUpdate = ftime;
update_camera_pos_BANG_();
ctx1.clearRect(0, 0, canvas2.width, canvas2.height);
texts.set_text_info_BANG_();
ctx1.fillText(1 / squint_core.get(gs.game_state, "dt"), 50, 30);
ctx1.fillText(squint_core.get(gs.game_state, "activeword"), 50, 50);
ctx1.fillText(squint_core.get(gs.game_state, "targetnode"), 50, 70);
if (squint_core.not(squint_core.get(gs.game_state, "mousedown"))) {
gs.game_state.activeword = null;
};
for (let G__3 of squint_core.iterable(squint_core.map_indexed(squint_core.vector, squint_core.get(gs.game_state, "texts")))) {
const vec__47 = G__3;
const idx8 = squint_core.nth(vec__47, 0, null);
const element9 = squint_core.nth(vec__47, 1, null);
for (let G__10 of squint_core.iterable(element9.drawdata.words)) {
const word11 = G__10;
if ((gs.game_state.targetnode) !== (idx8)) {
ctx1.globalAlpha = 0.5;
} else {
ctx1.globalAlpha = 1;
};
const wordx12 = word11.xpos;
const wordy13 = word11.ypos;
const xpos14 = world__GT_screen("x", (element9.xpos) + (wordx12));
const ypos15 = world__GT_screen("y", (element9.ypos) + (wordy13));
if (squint_core.truth_(point_in_rect_QMARK_(squint_core.get(gs.game_state, "mouseX"), squint_core.get(gs.game_state, "mouseY"), xpos14, ypos15, word11.width, word11.height))) {
if (squint_core.not(squint_core.get(gs.game_state, "mousedown"))) {
gs.game_state.targetnode = idx8;
gs.game_state.activeword = [idx8, word11];
ctx1.fillStyle = "red";
}} else {
ctx1.fillStyle = "black";
};
if (squint_core.truth_(on_screen_QMARK_(xpos14, ypos15, word11.width, word11.height))) {
ctx1.fillText(squint_core.get(word11, "word"), xpos14, ypos15)}
}
};
if (squint_core.truth_((() => {
const and__24283__auto__16 = gs.game_state.activeword;
if (squint_core.truth_(and__24283__auto__16)) {
return gs.game_state.mousedown} else {
return and__24283__auto__16}
})())) {
ctx1.beginPath();
ctx1.moveTo(gs.game_state.strokestartX, gs.game_state.strokestartY);
ctx1.lineTo(gs.game_state.mouseX, gs.game_state.mouseY);
ctx1.stroke()};
for (let G__17 of squint_core.iterable(gs.game_state.strokes)) {
const stroke18 = G__17;
const sx19 = world__GT_screen("x", stroke18.sx);
const sy20 = world__GT_screen("y", stroke18.sy);
const ex21 = world__GT_screen("x", stroke18.ex);
const ey22 = world__GT_screen("y", stroke18.ey);
ctx1.beginPath();
ctx1.moveTo(sx19, sy20);
ctx1.lineTo(ex21, ey22);
ctx1.stroke()
};
return window.requestAnimationFrame(main)
};
var markovfy_kjv_BANG_ = function () {
const words1 = kjv.kjv.split(/\s/);
const table2 = gs.game_state.transitiontable;
const pairs3 = squint_core.partition(2, 1, words1);
for (let G__4 of squint_core.iterable(pairs3)) {
const vec__58 = G__4;
const word19 = squint_core.nth(vec__58, 0, null);
const word210 = squint_core.nth(vec__58, 1, null);
const inner_map11 = ((squint_core.truth_(table2.has(word19))) ? (table2.get(word19)) : (new Map()));
inner_map11.set(word210, ((squint_core.truth_(inner_map11.has(word210))) ? ((inner_map11.get(word210) + 1)) : (1)));
table2.set(word19, inner_map11)
}return null
};
var weighted_random_choice = function (weights) {
const entries1 = Array.from(weights.entries());
const total2 = squint_core.reduce(squint_core._PLUS_, squint_core.map(squint_core.second, entries1));
if ((total2 > 0)) {
const r3 = (Math.random()) * (total2);
const data4 = ({ "cumulative": 0, "index": 0 });
while(true){
if ((data4.index) >= (entries1.length)) {
return null} else {
const vec__58 = entries1.at(data4.index);
const k9 = squint_core.nth(vec__58, 0, null);
const v10 = squint_core.nth(vec__58, 1, null);
if (((data4.cumulative) + (v10)) >= (r3)) {
return k9} else {
data4.cumulative = (data4.cumulative) + (v10);
data4.index = (data4.index + 1);
continue;
}};break;
}
}
};
var load_kjv_BANG_ = function () {
const lines1 = kjv.kjv.split("\n");
const choice2 = squint_core.rand_nth(lines1);
markovfy_kjv_BANG_();
gs.game_state.lines = lines1;
return gs.game_state.texts.push(texts.create_text_node(choice2, 0, 0))
};
var begin_stroke_BANG_ = function (e) {
if (squint_core.truth_(gs.game_state.activeword)) {
gs.game_state.strokestartX = e.clientX;
return gs.game_state.strokestartY = e.clientY;
}
};
var end_stroke_BANG_ = function (e) {
const sxpos1 = screen__GT_world("x", gs.game_state.strokestartX);
const sypos2 = screen__GT_world("y", gs.game_state.strokestartY);
const expos3 = screen__GT_world("x", e.clientX);
const eypos4 = screen__GT_world("y", e.clientY);
if (squint_core.truth_(gs.game_state.activeword)) {
squint_core.println(gs.game_state.transitiontable.get(gs.game_state.activeword));
squint_core.println(weighted_random_choice(gs.game_state.transitiontable.get(gs.game_state.activeword)));
return gs.game_state.strokes.push(({ "sx": sxpos1, "sy": sypos2, "ex": expos3, "ey": eypos4 }))}
};
var init = function () {
const canvas1 = create_canvas_BANG_("canv");
const context2 = create_context_BANG_(canvas1);
const ratio3 = window.devicePixelRatio;
const _4 = resize_canvas_BANG_(canvas1, context2, ratio3);
gs.game_state.canvas = canvas1;
gs.game_state.context = context2;
document.querySelector("#app").append(canvas1);
window.addEventListener("resize", squint_core.partial(resize_canvas_BANG_, canvas1, context2, ratio3), false);
canvas1.addEventListener("mouseleave", (function (_) {
return gs.game_state.mousedown = false;

}));
canvas1.addEventListener("mouseup", (function (e) {
end_stroke_BANG_(e);
return gs.game_state.mousedown = false;

}));
canvas1.addEventListener("mousedown", (function (e) {
begin_stroke_BANG_(e);
return gs.game_state.mousedown = true;

}));
canvas1.addEventListener("mousemove", (function (p__23) {
const map__56 = p__23;
const offsetX7 = squint_core.get(map__56, "offsetX");
const offsetY8 = squint_core.get(map__56, "offsetY");
gs.game_state.mouseX = offsetX7;
return gs.game_state.mouseY = offsetY8;

}));
load_kjv_BANG_();
return window.requestAnimationFrame(main)
};
init();

export { resize_canvas_BANG_, on_screen_QMARK_, create_canvas_BANG_, point_in_rect_QMARK_, update_camera_pos_BANG_, weighted_random_choice, begin_stroke_BANG_, end_stroke_BANG_, screen__GT_world, world__GT_screen, load_kjv_BANG_, main, init, create_context_BANG_, markovfy_kjv_BANG_ }

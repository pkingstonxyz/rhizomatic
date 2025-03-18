import * as squint_core from 'squint-cljs/core.js';
import * as texts from './texts.mjs';
import * as gs from './gamestate.mjs';
var create_canvas_BANG_ = function (id) {
const id_string1 = squint_core.str("#canvas_", id);
const try_canv2 = document.querySelector(id_string1);
const canv3 = ((squint_core.truth_(try_canv2)) ? (try_canv2) : (document.createElement("canvas")));
const w4 = window.innerWidth;
const h5 = window.innerHeight;
const ratio6 = window.devicePixelRatio;
squint_core.println(w4);
squint_core.println(h5);
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
var update_camera_pos_BANG_ = function () {
const targX1 = squint_core.get(squint_core.nth(squint_core.get(gs.game_state, "texts"), squint_core.get(gs.game_state, "targetnode")), "xpos");
const targY2 = squint_core.get(squint_core.nth(squint_core.get(gs.game_state, "texts"), squint_core.get(gs.game_state, "targetnode")), "ypos");
const cX3 = squint_core.get(gs.game_state, "cameraX");
const cY4 = squint_core.get(gs.game_state, "cameraY");
const dX5 = (targX1) - (cX3);
const dY6 = (targY2) - (cY4);
const dist7 = Math.sqrt(((dX5) * (dX5)) + ((dY6) * (dY6)));
const dt8 = squint_core.get(gs.game_state, "dt");
const smoothing9 = 5;
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
var draw_BANG_ = function (ctx, canvas) {
ctx.clearRect(0, 0, canvas.width, canvas.height);
texts.set_text_info_BANG_();
ctx.fillText(squint_core.get(gs.game_state, "activeword"), 50, 50);
ctx.fillText(squint_core.get(gs.game_state, "targetnode"), 50, 70);
ctx.fillText(1 / squint_core.get(gs.game_state, "dt"), 50, 30, 70);
for (let G__1 of squint_core.iterable(squint_core.map_indexed(squint_core.vector, squint_core.get(gs.game_state, "texts")))) {
const vec__25 = G__1;
const idx6 = squint_core.nth(vec__25, 0, null);
const element7 = squint_core.nth(vec__25, 1, null);
for (let G__8 of squint_core.iterable(element7.drawdata.words)) {
const word9 = G__8;
const wordx10 = word9.xpos;
const wordy11 = word9.ypos;
const cx12 = gs.game_state.cameraX;
const cy13 = gs.game_state.cameraY;
const xpos14 = ((wordx10) - (cx12)) + ((window.innerWidth) / (2)) + (element7.xpos);
const ypos15 = ((wordy11) - (cy13)) + ((window.innerHeight) / (2)) + (element7.ypos);
ctx.fillText(squint_core.get(word9, "word"), xpos14, ypos15)
}
}return null
};
var main = function (ftime) {
const ctx1 = squint_core.get(gs.game_state, "context");
const canvas2 = squint_core.get(gs.game_state, "canvas");
gs.game_state.dt = ((ftime) - (squint_core.get(gs.game_state, "lastUpdate"))) / (1000);
gs.game_state.lastUpdate = ftime;
update_camera_pos_BANG_();
draw_BANG_(ctx1, canvas2);
return window.requestAnimationFrame(main)
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
canvas1.addEventListener("mouseup", (function (_) {
return gs.game_state.mousedown = false;

}));
canvas1.addEventListener("mousedown", (function (_) {
return gs.game_state.mousedown = true;

}));
canvas1.addEventListener("mousemove", (function (p__45) {
const map__56 = p__45;
const offsetX7 = squint_core.get(map__56, "offsetX");
const offsetY8 = squint_core.get(map__56, "offsetY");
gs.game_state.mouseX = offsetX7;
return gs.game_state.mouseY = offsetY8;

}));
squint_core.get(gs.game_state, "texts").push(texts.create_text_node("hello my name is patrick. I hope this text is long enough to showcase the splitting!", 0, 0), texts.create_text_node("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce vehicula pharetra tristique. Praesent sed lectus tristique, vestibulum risus vel, condimentum sem. Morbi interdum, nisl id finibus commodo, felis tellus pulvinar velit, vitae convallis orci purus posuere urna. Cras venenatis lorem a vestibulum pulvinar. Proin pharetra aliquam metus, eget faucibus neque. Donec tincidunt lobortis nisi vitae fermentum. Aliquam iaculis mi scelerisque libero efficitur vestibulum. In hac habitasse platea dictumst. Proin efficitur, orci sed pretium sodales, odio ligula tempus libero, ut dapibus sapien nibh sit amet odio. Sed ac neque nec est auctor egestas sollicitudin ullamcorper velit. Duis malesuada ex dui, a porttitor est tincidunt at.", 100, 150));
return window.requestAnimationFrame(main)
};
init();

export { create_canvas_BANG_, create_context_BANG_, resize_canvas_BANG_, point_in_rect_QMARK_, update_camera_pos_BANG_, draw_BANG_, main, init }

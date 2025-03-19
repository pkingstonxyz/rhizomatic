import * as squint_core from 'squint-cljs/core.js';
import * as gs from './gamestate.mjs';
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
var distance_between = function (sx, sy, ex, ey) {
return Math.sqrt((((ex) - (sx)) * ((ex) - (sx))) + (((ey) - (sy)) * ((ey) - (sy))))
};

export { point_in_rect_QMARK_, on_screen_QMARK_, world__GT_screen, screen__GT_world, distance_between }

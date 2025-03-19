import * as squint_core from 'squint-cljs/core.js';
import * as gs from './gamestate.mjs';
var word_width = function (word_measurement) {
return (word_measurement.actualBoundingBoxLeft) + (word_measurement.actualBoundingBoxRight)
};
var set_text_info_BANG_ = function () {
const ctx1 = gs.game_state.context;
ctx1.textAlign = "start";
ctx1.textBaseline = "top";
ctx1.font = "18px serif";
return ctx1.fillStyle = "black";

};
var create_text_measurement = function (text) {
const data1 = ({ "drawdata": ({ "width": 0, "height": 0, "words": [] }), "linenum": 0, "workinglen": 0 });
const lineheight2 = 20;
const ctx3 = gs.game_state.context;
set_text_info_BANG_();
for (let G__4 of squint_core.iterable(text.split(" "))) {
const word5 = G__4;
const measurement6 = ctx3.measureText(word5);
const width7 = (2) + (word_width(measurement6));
const potential_next_width8 = (width7) + (data1.workinglen);
if ((potential_next_width8) > (gs.game_state.textwidth)) {
data1.linenum = (data1.linenum + 1);
data1.drawdata.words.push(({ "word": word5, "xpos": 0, "ypos": (lineheight2) * (data1.linenum), "width": width7, "height": 20 }));
data1.drawdata.height = (lineheight2) * ((data1.linenum + 1));
data1.drawdata.width = (((data1.drawdata.width) > (data1.workinglen)) ? (data1.drawdata.width) : (data1.workinglen));
data1.workinglen = width7;
} else {
data1.drawdata.words.push(({ "word": word5, "xpos": data1.workinglen, "ypos": (lineheight2) * (data1.linenum), "width": width7, "height": 20 }));
data1.workinglen = potential_next_width8;
}
};
for (let G__9 of squint_core.iterable(data1.drawdata.words)) {
const word10 = G__9;
const adjustx11 = (data1.drawdata.width) / (2);
const adjusty12 = (data1.drawdata.height) / (2);
word10.xpos = (word10.xpos) - (adjustx11);
word10.ypos = (word10.ypos) - (adjusty12);

};
return data1.drawdata
};
var create_text_node = function (value, xpos, ypos) {
const node1 = ({ "text": value, "xpos": xpos, "ypos": ypos, "drawdata": create_text_measurement(value) });
squint_core.println(node1);
return node1
};
var remeasure_text_nodes_BANG_ = function () {
for (let G__1 of squint_core.iterable(gs.game_state.texts)) {
const node2 = G__1;
const text3 = node2.text;
const remeasured4 = create_text_measurement(text3);
node2.drawdata = remeasured4;

}return null
};

export { word_width, set_text_info_BANG_, create_text_measurement, create_text_node, remeasure_text_nodes_BANG_ }

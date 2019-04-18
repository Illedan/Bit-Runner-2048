import * as utils from '../core/utils.js';
import { api as entityModule } from '../entity-module/GraphicEntityModule.js';

export const api2 = {
  showDebug: false
}

export class DebugModule {
  constructor (assets) {
  }

  static get name () {
    return 'debug'
  }

  updateScene (previousData, currentData, progress) {
    for(let id of this.registrations){
        var entity = entityModule.entities.get(id);
        entity.container.visible = api2.showDebug;
    }
  }

  handleFrameData (frameInfo, newRegistrations) {
    this.registrations = this.registrations.concat(newRegistrations);
    return {...frameInfo}
  }


  generateText(text, size, color, align) {
    var textEl = new PIXI.Text(text, {
      fontSize: Math.round(size / 1.2) + 'px',
      fontFamily: 'Lato',
      fontWeight: 'bold',
      fill: color
    });

    textEl.lineHeight = Math.round(size / 1.2);
    if (align === 'right') {
      textEl.anchor.x = 1;
    } else if (align === 'center') {
      textEl.anchor.x = 0.5;
    }

    return textEl;
  };

  reinitScene (container, canvasData) {
  //      var tooltip = new PIXI.Container();
  //    var background = tooltip.background = new PIXI.Graphics();
  //    var label = tooltip.label = this.generateText('', 36, 0xFFFFFF, 'left');
//
  //    var oneDay = 24*60*60*1000; // hours*minutes*seconds*milliseconds
  //    var firstDate = new Date(2019,05, 12);
  //    var secondDate = new Date();
//
  //    var diffDays = Math.round(Math.abs((firstDate.getTime() - secondDate.getTime())/(oneDay)));
//
  //      tooltip.label.text = "Days left: " + diffDays;
  //    background.beginFill(0x0, 0.7);
  //    background.drawRect(0, 0, 200, 185);
  //    background.endFill();
  //    background.x = -10;
  //    background.y = -10;
//
  //    tooltip.visible = true;
  //    tooltip.inside = {};
//
  //    tooltip.interactiveChildren = false;
//
  //    tooltip.addChild(background);
  //    tooltip.addChild(label);
  //    tooltip.y = 1020;
  //    tooltip.x = 900;
  //      container.addChild(tooltip);

  }

  animateScene (delta) {
  }

  handleGlobalData (players) {
  this.registrations = [];
  }
}
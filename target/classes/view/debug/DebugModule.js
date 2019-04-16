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

  reinitScene (container, canvasData) {
  }

  animateScene (delta) {
  }

  handleGlobalData (players) {
  this.registrations = [];
  }
}
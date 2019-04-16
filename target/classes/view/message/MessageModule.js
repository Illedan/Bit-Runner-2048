import * as utils from '../core/utils.js';
import { api as entityModule } from '../entity-module/GraphicEntityModule.js';

export const api = {
  showMessages: true
}

export class MessageModule {
  constructor (assets) {
  }

  static get name () {
    return 'message'
  }

  updateScene (previousData, currentData, progress) {
    for(let id of this.messageIds){
        var entity = entityModule.entities.get(id);
        entity.container.visible = api.showMessages;
    }
  }

  handleFrameData (frameInfo, nothing) {
    return {...frameInfo}
  }

  reinitScene (container, canvasData) {
  }

  animateScene (delta) {
  }

  handleGlobalData (players, messageIds) {
    this.messageIds = messageIds;
  }
}
import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { TooltipModule } from './tooltips/TooltipModule.js';
import { EndScreenModule } from './endscreen-module/EndScreenModule.js';
import { MessageModule, api } from './message/MessageModule.js';
import { DebugModule, api2 } from './debug/DebugModule.js';

// List of viewer modules that you want to use in your game
export const modules = [
	GraphicEntityModule, TooltipModule, EndScreenModule, MessageModule, DebugModule
];

export const gameName = 'Bit Runner 2048';


export const options = [{
  title: 'SHOW MESSAGES',
  get: function () {
    return api.showMessages
  },
  set: function (value) {
    api.showMessages = value
  },
  values: {
    'ON': true,
    'OFF': false
  }
},
{
  title: 'DEBUG',
  get: function () {
    return api2.showDebug
  },
  set: function (value) {
    api2.showDebug = value
  },
  values: {
    'ON': true,
    'OFF': false
  }
}
]

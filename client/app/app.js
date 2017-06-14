require('./bootstrap');

if (isPolyfillNeeded()) {
  require.ensure(['babel-polyfill', 'whatwg-fetch'], () => {
    require('babel-polyfill');
    require('whatwg-fetch');

    initialize();
  });
} else {
  initialize();
}

require.ensure(['./styles.less'], () => {
  require('./styles.less');
});

function isPolyfillNeeded() {
  return !window.Symbol || !Array.prototype.find;
}

function initialize() {
  const {Main, reducer} = require('main');
  const {init} = require('./init');

  init(Main, reducer);
}

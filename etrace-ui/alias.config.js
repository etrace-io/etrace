const path = require('path');

module.exports = {
  $components: path.resolve(__dirname, './src/components/'),
  $containers: path.resolve(__dirname, './src/containers/'),
  $constants: path.resolve(__dirname, './src/constants/'),
  $services: path.resolve(__dirname, './src/services/'),
  $styles: path.resolve(__dirname, './src/styles'),
  $models: path.resolve(__dirname, './src/models'),
  $utils: path.resolve(__dirname, './src/utils'),
  $store: path.resolve(__dirname, './src/store'),
  $hooks: path.resolve(__dirname, './src/hooks'),
  $asset: path.resolve(__dirname, './src/asset'),
};

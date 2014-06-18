requirejs.config({
    baseUrl: "/admin/assets/js",
    paths: {
      jquery: "//cdn.jsdelivr.net/jquery/1.7.1/jquery.min",
      angularjs: "//ajax.googleapis.com/ajax/libs/angularjs/1.0.5/angular.min",
      sanitize: "//ajax.googleapis.com/ajax/libs/angularjs/1.0.5/angular-sanitize.min",
      bootstrap: "lib/bootstrap.min",

      core: "app/core",
      admin: "app/admin",
      archdesc: "app/archdesc",
      lib : "lib"
    },
    shim: {
        /* Core libraries */
        'jquery': { exports : '$' },
        'bootstrap': { deps: ['jquery'] },

        /* Angular JS */
        'angularjs' : { 
          deps : ["jquery"], 
          exports: 'angular'
        },
        'sanitize' : { 
          deps : ["angularjs"], 
          exports: 'angular'
        },
        'lib/angular-bootstrap' : { 
          deps : ["angularjs", "sanitize"],
          exports : "angular"
        },

        /* Access Point Linker */
        'archdesc/accesspoints.factory'  : { deps : ["jquery", "angularjs", "sanitize", "lib/angular-bootstrap"]},
        'archdesc/accesspoints.main'  : { 
          deps : ["jquery", "angularjs", "sanitize", "lib/angular-bootstrap" , "archdesc/accesspoints.factory"],
          exports : 'application'
        },
        'archdesc/accesspoints.controllers'  : { deps : ["jquery", "angularjs", "sanitize", "lib/angular-bootstrap", "archdesc/accesspoints.main"]},

    },
    optimize: "uglify"
})

require(["angularjs", "archdesc/accesspoints.main"] , function(angular) {
});
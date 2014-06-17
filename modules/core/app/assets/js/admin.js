//var requirejs = require('requirejs');


({
    baseUrl: "./",
    paths: {
      core: "./app/core",
      admin: "./app/admin",
      jquery: "./lib/jquery"
    },
    include: [
    	"core/form.markdown",
    	"core/quiteview",
    	"core/stickyform",

    	"admin/form.tooltip",
    	"admin/form.misc"
    ],
    optimize: "uglify"
})

//requirejs(["app/quiteview"])
/*
requirejs.optimize(config, function (buildResponse) {
    //buildResponse is just a text output of the modules
    //included. Load the built file for the contents.
    //Use config.out to get the optimized file contents.
    var contents = fs.readFileSync(config.out, 'utf8');
}, function(err) {
    //optimization err callback
});*/
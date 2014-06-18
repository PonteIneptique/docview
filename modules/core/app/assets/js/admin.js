//var requirejs = require('requirejs');


requirejs.config({
    baseUrl: "/admin/assets/js",
    paths: {
      jquery: "lib/jquery",
      bootstrap: "lib/bootstrap.min",
      selectTwo: "lib/select2.min",
      datepicker: "lib/bootstrap-datepicker",

      core: "app/core",
      admin: "app/admin"
    },
    shim: {
        /* Core libraries */
        'jquery': { exports : '$' },
        'bootstrap': { deps: ['jquery'] },
        'selectTwo': { deps: ['jquery'] },

        /* Core functionnalities */
        "core/form.markdown": { deps: ['jquery', 'bootstrap'] },
        "core/quiteview": { deps: ['jquery'] },
        "core/stickyform": { deps: ['jquery'] },

        /* Admin functionnalities  */
        "admin/form.tooltip": { deps: ['jquery', 'bootstrap'] },
        "admin/form.misc": { deps: ['jquery', 'bootstrap', 'selectTwo'] },

        /* Date picker stuff*/
        'datepicker': { deps: ['jquery', 'bootstrap'] },
        "admin/form.datepicker": { deps: ['jquery', 'datepicker', 'bootstrap'] }
    },
    optimize: "uglify"
})

require(['core/form.markdown', 'core/quiteview', 'core/stickyform', 'admin/form.tooltip', 'admin/form.misc', "admin/form.datepicker"], function() {
    console.log("Admin.js loaded")
});
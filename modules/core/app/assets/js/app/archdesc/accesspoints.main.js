define(["angularjs"], function(angular) {

  /*
  *
  *
  *     PROPER APPLICATION
  *
  *
  */


  var linker = angular.module('AccessPointLinker', ["ngSanitize", "dialog", 'ui.bootstrap.modal', 'ui.bootstrap.typeahead', 'ui.bootstrap.pagination' ], function($provide) {
    $provide.factory('$search', function($http, $log, $service) {
      var search = function(types, searchTerm, page) {
        var params = "?limit=10&q=" + (searchTerm || "");
        if (types && types.length > 0) {
          if(Array.isArray(types)) {
            params = params + "&" + (types.map(function(t) { return "st[]=" + t }).join("&"));
          } else {
            params = params + "&st[]=" + types;
          }
        }
        if (page > 0) {
          params = params + "&page=" + page;
        }
        return $http.get($service.filter().url + params);
      };
      var limitTypes = function(type) {
        if(Array.isArray(type)) {
          return type;
        }
        if (type.match(/(?:creator|person)Access/)) {
          return ["historicalAgent"];
        }
        if (type.match(/(?:place)Access/)) {
          return ["cvocConcept", "country"];
        }
        if (type === "subjectAccess") {
          return ["cvocConcept"];
        }
        return [];
      };
      return {
        search: function(type, searchTerm, page) {
          return search(this.limitTypes(type), searchTerm, page);
        },
        limitTypes : limitTypes,
        filter: function(type, searchTerm, page) {
          return search(this.limitTypes(type), (searchTerm || "PLACEHOLDER_NO_RESULTS"), page);
        },

        detail: function(type, id) {
          return $http.get($service.getItem(type, id).url, {
            headers: {
              "Content-Type": "application/json",
              "Accept": "application/json; charset=utf-8"
            }
          });
        }
      };
    });

    $provide.factory('$service', function() {
      return {
        get: jsRoutes.controllers.core.Application.get,
        getItem: jsRoutes.controllers.core.ApiController.getItem,
        filter: jsRoutes.controllers.core.SearchFilter.filter,
        createLink: jsRoutes.controllers.archdesc.DocumentaryUnits.createLink,
        createMultipleLinks: jsRoutes.controllers.archdesc.DocumentaryUnits.linkMultiAnnotatePost,
        createAccessPoint: jsRoutes.controllers.archdesc.DocumentaryUnits.createAccessPoint,
        getAccessPoints: jsRoutes.controllers.archdesc.DocumentaryUnits.getAccessPointsJson,
        deleteLink: jsRoutes.controllers.archdesc.DocumentaryUnits.deleteLink,
        deleteAccessPoint: jsRoutes.controllers.archdesc.DocumentaryUnits.deleteAccessPoint,
        deleteLinkAndAccessPoint: jsRoutes.controllers.archdesc.DocumentaryUnits.deleteLinkAndAccessPoint,
        redirectUrl: function(id) {
          return jsRoutes.controllers.archdesc.DocumentaryUnits.get(id).url;
        }
      };
    });

    $provide.factory('$names', function() {
      return {
        cvocConcept: "Concept/Keyword",
        cvocVocabulary: "Vocabulary",
        documentaryUnit: "Archival Unit",
        repository: "Repository",
        historicalAgent: "Authority"
      }
    });

  });

  //
  // Directives for testing, these aren't used yet...
  //
  linker.directive('ngEnter', function() {
    return function(scope, elem, attrs) {
      elem.bind('keypress', function(e) {
        if (e.charCode === 13) scope.$apply(attrs.ngEnter);
      });
    };
  });

  linker.directive('ngKeyNav', function() {
    return function(scope, elem, attrs) {
      elem.bind('keypress', function(e) {
        if (e.charCode === 38) scope.$apply(attrs.ngKeyNav);
        else if (e.charCode === 40) scope.$apply(attrs.ngKeyNav);
      });
    };
  });
  linker.directive('selectOnClick', function() {
    // Linker function
    return function(scope, element, attrs) {
      element.click(function() {
        element.select();
      });
    };
  });
})
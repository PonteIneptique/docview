var SAVE = portal.controller('SaveCtrl', ['$scope', 'portal', '$filter', 'ui', '$http', function($scope, $portal, $filter, $ui, $http) {

	$scope.ui = {
		bookmark : {
			object : { },
			get : function() {
				this.object = $ui.bookmark.object("bookmark");
				console.log(this.object);
			}
		}
	}
	
	//<-- Load data 
	$scope.ui.bookmark.get();
	$ui.title("Saved items");
	// Load data-->
}]);
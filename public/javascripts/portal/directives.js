portal.directive('whenScrolled', function ($window) {
    return function(scope, element, attrs) {
        angular.element($window).bind("scroll", function() {
		
			var bottomPos = parseInt(element[0].offsetTop) + parseInt(element[0].offsetHeight) + 95;
			if (document.documentElement.scrollTop) { var currentScroll = document.documentElement.scrollTop; } else { var currentScroll = document.body.scrollTop; }
			var totalHeight = document.body.offsetHeight;
			var visibleHeight = document.documentElement.clientHeight;
			
			// Particularities : we have margin on top and bot
			//Bottom = 65
			//Top = 95
			
			//Total height of doc = (currentScroll + visibleHeight + 150)
			//Div's Bottom position = (element[0].offsetTop + element[0].offsetHeight)
			//Take care of calling it in angular.element. In any other case you would have its height before datas has been loaded
			
			var currentScrollHeight = currentScroll + visibleHeight + 95;
             if (bottomPos  <= currentScrollHeight) {
				scope.$apply(attrs.whenScrolled);
             }
        });
    }
}).directive("drop", ['$rootScope', function($rootScope) {
  
  function dragEnter(evt, element, dropStyle) {
    evt.preventDefault();
    element.addClass(dropStyle);
  };
  function dragLeave(evt, element, dropStyle) {
    element.removeClass(dropStyle);
  };
  function dragOver(evt) {
    evt.preventDefault();
  };
  function drop(evt, element, dropStyle) {
    evt.preventDefault();
    element.removeClass(dropStyle);
  };
  
  return {
    restrict: 'A',
    link: function(scope, element, attrs)  {
      scope.dropData = scope[attrs["drop"]];
      scope.dropStyle = attrs["dropstyle"];
      element.bind('dragenter', function(evt) {
        dragEnter(evt, element, scope.dropStyle);
      });
      element.bind('dragleave', function(evt) {
        dragLeave(evt, element, scope.dropStyle);
      });
      element.bind('dragover', dragOver);
      element.bind('drop', function(evt) {
        drop(evt, element, scope.dropStyle);
        $rootScope.$broadcast('dropEvent', $rootScope.draggedElement, scope.dropData);
      });
    }
  }
}]).directive("drag", ["$rootScope", function($rootScope) {
  
  function dragStart(evt, element, dragStyle) {
    element.addClass(dragStyle);
    evt.dataTransfer.setData("id", evt.target.id);
    evt.dataTransfer.effectAllowed = 'move';
  };
  function dragEnd(evt, element, dragStyle) {
    element.removeClass(dragStyle);
  };
  
  return {
    restrict: 'A',
    link: function(scope, element, attrs)  {
      attrs.$set('draggable', 'true');
      scope.dragData = scope[attrs["drag"]];
      scope.dragStyle = attrs["dragstyle"];
      element.bind('dragstart', function(evt) {
        $rootScope.draggedElement = scope.dragData;
        dragStart(evt, element, scope.dragStyle);
      });
      element.bind('dragend', function(evt) {
        dragEnd(evt, element, scope.dragStyle);
      });
    }
  }
}]).directive("annotate", ['$rootScope', function($rootScope) {
  function getSelected(evt) {
	if (window.getSelection) {
		ret =  window.getSelection().toString();
	} else if (document.selection) {
		ret = document.selection.createRange().text;
	}
	if(ret) { return ret; }
	else { return false; }
  };
  
  return {
    restrict: 'A',
    link: function(scope, element, attrs)  {
      scope.annotateData = scope[attrs["annotate"]];
      element.bind('mouseup', function(evt) {
        scope.selectedText = getSelected(evt);
		//console.log(scope.selectedText);
		//console.log(scope.annotateData);
		if(scope.selectedText != false)
		{
			$rootScope.$broadcast('getAnnotation', scope.annotateData, scope.selectedText);
		}
      });
    }
  }
}]).directive('sap', function() {
    return {
        restrict: 'E',
        replace: true,
        template: '<div></div>',
        link: function(scope, element, attrs) {
				//console.log(attrs);
				//console.log(attrs.lat);
				//console.log(scope.geoloc.lat);
            var map = L.map(attrs.id, {
                center: [scope.geoloc.lat, scope.geoloc.lon],
                zoom: 10
            });
			console.log(map);
            //create a CloudMade tile layer and add it to the map
            L.tileLayer('http://{s}.tile.cloudmade.com/57cbb6ca8cac418dbb1a402586df4528/997/256/{z}/{x}/{y}.png', {
                maxZoom: 18
            }).addTo(map);

            //add markers dynamically
           var points = [{lat: scope.geoloc.lat, lng: scope.geoloc.lon}];
            for (var p in points) {
                L.marker([points[p].lat, points[p].lng]).addTo(map);
            }
        }
    };
});
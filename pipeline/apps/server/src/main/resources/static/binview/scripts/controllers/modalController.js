/**
 * Created by matthewmueller on 9/14/17.
 */

 angular.module('app').controller('ModalInstanceCtrl', function ($scope, $uibModalInstance, items) {
   var $ctrl = this;
   $ctrl.items = items;

   $ctrl.ok = function () {
     $uibModalInstance.close($scope.groupName);
   };

   $ctrl.cancel = function () {
     $uibModalInstance.dismiss('cancel');
   };
 });

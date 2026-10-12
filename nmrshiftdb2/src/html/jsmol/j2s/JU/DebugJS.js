Clazz.declarePackage("JU");
(function(){
var c$ = Clazz.declareType(JU, "DebugJS", null);
c$._ = Clazz.defineMethod(c$, "_", 
function(msg){
{
if (Clazz._debugging) {
debugger;
}
}}, "~S");
})();
;//5.0.1-v2 Sun May 05 10:20:01 CDT 2024

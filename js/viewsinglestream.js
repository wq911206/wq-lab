$(document).ready(function(){
    var ss=$('file');
    $('button').click(function(){
        $('p').append("wq")
        $('p').append(ss.files[0].name)
    });
});
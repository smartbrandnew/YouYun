var utils = {};

utils.captureMouse = function (element) {
    var touch = {x: null, y :null, isPressed: false};

    element.addEventListener('touchstart', function (event) {
        touch.isPressed = true;
    }, false);

    element.addEventListener('touchend', function (event) {
        touch.isPressed = true;
        touch.x = null;
        touch.y = null;
    }, false);

    element.addEventListener('touchmove', function (event) {
        var x,y,touch_event = event.touches[0];

        if (touch_event.pageX | touch_event.pageY) {
            x = touch_event.pageX;
            y = touch_event.pageY;
        } else {
            x = touch_event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
            x = touch_event.clientX + document.body.scrollLeft + document.documentElement.scrollTop;
        }
        x -= offsetLeft;
        y -= offsetTop;

        touch.x = x;
        touch.y = y;
    }, false);

    return touch;
}
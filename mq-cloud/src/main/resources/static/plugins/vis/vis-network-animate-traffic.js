vis.Network.prototype.animateTraffic =
    function(edgesTrafficList,
             onPreAnimationHandler,
             onPreAnimateFrameHandler,
             onPostAnimateFrameHandler,
             onPostAnimationHandler) {

        var thisAnimator  = this;

        var trafficAnimator = {

            thisNetwork : this,

            trafficCanvas : null,
            trafficCanvasCtx : null,
            trafficCanvasWidth : null,
            trafficCanvasHeight: null,

            reportedErrors : {},

            edgesTrafficList : edgesTrafficList ,

            onPreAnimateFrame  : onPreAnimateFrameHandler,
            onPostAnimateFrame: onPostAnimateFrameHandler,
            onPreAnimation : onPreAnimationHandler,
            onPostAnimation : onPostAnimationHandler,

            // return object {edge, trafficSize, isBackward}
            parseEdgeTraffic : function (edgeTraffic) {
                var edge;
                if (edgeTraffic.edge) {
                    edge =  edgeTraffic.edge.edgeType
                        ? edgeTraffic.edge
                        : this.thisNetwork.body.edges[edgeTraffic.edge.id]
                        || this.thisNetwork.body.edges[edgeTraffic.edge]
                    ;
                }
                else {
                    edge = this.thisNetwork.body.edges[edgeTraffic];
                }

                return {
                    edge: edge,
                    trafficSize : edgeTraffic.trafficSize || 1,
                    isBackward : edge && edgeTraffic.isBackward
                };
            },

            clearAnimationCanvas : function () {
                this.trafficCanvasCtx.save();
                this.trafficCanvasCtx.setTransform(1, 0, 0, 1, 0, 0);
                this.trafficCanvasCtx.clearRect(0,0, this.trafficCanvasWidth, this.trafficCanvasHeight);
                this.trafficCanvasCtx.restore();
            },

            getNetworkTrafficCanvas : function() {

                this.trafficCanvas = this.thisNetwork.body
                    .container.getElementsByClassName('networkTrafficCanvas')[0];

                if ( this.trafficCanvas == undefined) {

                    var frame = this.thisNetwork.canvas.frame;
                    this.trafficCanvas = document.createElement('canvas');
                    this.trafficCanvas.className = 'networkTrafficCanvas';
                    this.trafficCanvas.style.position = 'absolute';
                    this.trafficCanvas.style.top = this.trafficCanvas.style.left = 0;
                    this.trafficCanvas.style.zIndex = 1;
                    this.trafficCanvas.style.pointerEvents='none';
                    this.trafficCanvas.style.width = frame.style.width;
                    this.trafficCanvas.style.height = frame.style.height;
                    this.trafficCanvas.width = frame.canvas.clientWidth;
                    this.trafficCanvas.height = frame.canvas.clientHeight;

                    frame.appendChild(this.trafficCanvas);
                }

                return this.trafficCanvas;
            },

            animateFrame : function (offset, frameCounter) {

                this.clearAnimationCanvas();

                var maxOffset = .9;

                var reportedError = {};

                if (offset > maxOffset) {
                    if (this.onPostAnimation) this.onPostAnimation(this.edgesTrafficList);
                    return;
                }
                for(var i in this.edgesTrafficList) {

                    var edgeTraffic = this.parseEdgeTraffic(this.edgesTrafficList[i]);

                    if (!edgeTraffic.edge) {
                        if (!this.reportedErrors[this.edgesTrafficList[i]]) {
                            console.error ("No edge path defined: " , this.edgesTrafficList[i]);
                            this.reportedErrors[this.edgesTrafficList[i]] = true;
                        }
                        continue;
                    }

                    if (this.onPreAnimateFrameHandler
                        && this.onPreAnimateFrameHandler(edgeTraffic,frameCounter) === false ) {
                        continue;
                    }

                    var p = edgeTraffic.edge.edgeType.getPoint(
                        edgeTraffic.isBackward ? maxOffset - offset: offset);

                    this.trafficCanvasCtx.beginPath();
                    this.trafficCanvasCtx.arc(p.x, p.y, parseInt(edgeTraffic.trafficSize) || 1, 0, Math.PI*2, false);
                    this.trafficCanvasCtx.lineWidth=1;
                    this.trafficCanvasCtx.strokeWidth=4;
                    this.trafficCanvasCtx.strokeStyle="rgba(57,138,255,0.1)";
                    this.trafficCanvasCtx.fillStyle = "rgb(255,182,0)";
                    this.trafficCanvasCtx.fill();
                    this.trafficCanvasCtx.stroke();
                    this.trafficCanvasCtx.closePath();

                    if (this.onPostAnimateFrame
                        && this.onPostAnimateFrame(edgeTraffic,frameCounter) === false) {
                        if (this.onPostAnimation) this.onPostAnimation(this.edgesTrafficList);
                        return;
                    }

                }

                setTimeout(this.animateFrame.bind(this), 10, offset+.01, frameCounter++);
            },


            initalizeCanvasForEdgeAnimation : function () {

                this.reportedErrors = {};

                if (Object.prototype.toString.call( this.edgesTrafficList ) !== '[object Array]') {
                    this.edgesTrafficList = [this.edgesTrafficList];
                }

                this.trafficCanvas = this.getNetworkTrafficCanvas();

                this.trafficCanvasCtx = this.trafficCanvas.getContext('2d');
                this.trafficCanvasWidth = this.trafficCanvasCtx.canvas.width;
                this.trafficCanvasHeight= this.trafficCanvasCtx.canvas.height;

                var edgeTraffic = this.parseEdgeTraffic(this.edgesTrafficList[0]);

                var s = this.thisNetwork.getScale();// edgeTraffic.edge.body.view.scale;
                var t = this.thisNetwork.body.view.translation; //edgeTraffic.edge.body.view.translation;

                this.trafficCanvasCtx.setTransform(1, 0, 0, 1, 0, 0);
                this.trafficCanvasCtx.translate(t.x, t.y);
                this.trafficCanvasCtx.scale(s, s);
            },

        };
        trafficAnimator.initalizeCanvasForEdgeAnimation();
        if (trafficAnimator.onPreAnimation
            && trafficAnimator.onPreAnimation(trafficAnimator.edgesTrafficList) === false) return;
        trafficAnimator.animateFrame( 0.1 /*animationStartOffset*/, 0 /*frame*/);
    };


vis.Network.prototype.animateTrafficOnPostAnimation = function(edgesTrafficList) {
    // add the value from the source traffic to target
    for (var i in edgesTrafficList) {
        edgeTraffic = this.parseEdgeTraffic(edgesTrafficList[i]);
        if (!edgeTraffic.edge) {
            continue;
        }
        var toValue = edgeTraffic.edge.to.getValue()
        if (parseFloat(toValue)) {
            var newValue = (toValue || 0)
                + (edgeTraffic.isBackward ? -1 : 1) * edgeTraffic.trafficSize;
            this.thisNetwork.body.data.nodes
                .update({id:edgeTraffic.edge.toId, value: newValue});
        }
    }
};

vis.Network.prototype.animateTrafficOnPreAnimation = function(edgesTrafficList) {
    // remove the value from the source traffic
    for (var i in edgesTrafficList) {
        edgeTraffic = this.parseEdgeTraffic(edgesTrafficList[i]);
        if (!edgeTraffic.edge) {
            continue;
        }
        var fromValue = edgeTraffic.edge.from.getValue()
        if (parseFloat(fromValue)) {

            var newValue = fromValue +
                (edgeTraffic.isBackward ? -1 : 1) * -edgeTraffic.trafficSize;

            this.thisNetwork.body.data.nodes
                .update({id:edgeTraffic.edge.fromId, value:Math.max(0, newValue)});
        }
    }
};
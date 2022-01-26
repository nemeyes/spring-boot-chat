<!doctype html>
<html lang="en">
  <head>
    <title>SeersChat</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <!-- CSS -->
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/dist/css/bootstrap.min.css">
    <style>
      [v-cloak] {
          display: none;
      }
    </style>
  </head>
  <body>
  	<div class="container" id="login" v-if="blogin">
		<h2 class="form-signin-heading">Please sign in</h2>
		<p>
			<label for="username" class="sr-only">Login ID</label>
			<input type="text" class="form-control" v-model="user_id" placeholder="User ID" required autofocus>
		</p>
		<p>
  			<label for="password" class="sr-only">Login Password</label>
  			<input type="password" class="form-control" v-model="user_password" placeholder="User Password" required>
		</p>
		<button class="btn btn-lg btn-primary btn-block" @click="signin">로그인 하기</button>
		<button class="btn btn-lg btn-primary btn-block" onclick="location.href='/user/signup'">가입 하기</button>
	</div>
    <div class="container" id="chatroom" v-if="bchatroom" v-cloak>
        <div class="row">
            <div class="col-md-6">
                <h3>채팅방 리스트</h3>
            </div>
            <div class="col-md-6 text-right">
                <a class="btn btn-primary btn-sm" href="/logout">로그아웃</a>
            </div>
        </div>
        <div class="input-group">
            <div class="input-group-prepend">
                <label class="input-group-text">방제목</label>
            </div>
            <input type="text" class="form-control" v-model="room_name" v-on:keyup.enter="createRoom">
            <div class="input-group-append">
                <button class="btn btn-primary" type="button" @click="createRoom">채팅방 개설</button>
            </div>
        </div>
        <ul class="list-group">
            <li class="list-group-item list-group-item-action" v-for="item in chatrooms" v-bind:key="item.roomId" v-on:click="enterRoom(item.roomId, item.roomName)">
                <h6>{{item.roomName}} <span class="badge badge-info badge-pill">{{item.zCount}}</span></h6>
            </li>
        </ul>
    </div>
    <div class="container" id="chat" v-if="bchat"  v-cloak>
        <div class="row">
            <div class="col-md-6">
                <h4>{{roomName}} <span class="badge badge-info badge-pill">{{userCount}}</span></h4>
            </div>
            <div class="col-md-6 text-right">
                <a class="btn btn-primary btn-sm" href="/logout">로그아웃</a>
                <a class="btn btn-info btn-sm" href="/chat/room">채팅방 나가기</a>
            </div>
        </div>
        <div class="input-group">
            <div class="input-group-prepend">
                <label class="input-group-text">내용</label>
            </div>
            <input type="text" class="form-control" v-model="message" v-on:keypress.enter="sendMessage('MSG_TALK')">
            <div class="input-group-append">
                <button class="btn btn-primary" type="button" @click="sendMessage('MSG_TALK')">보내기</button>
            </div>
        </div>
        <ul class="list-group">
            <li class="list-group-item" v-for="message in messages">
                {{message.sender}} - {{message.message}}</a>
            </li>
        </ul>
    </div>    
    <!-- JavaScript -->
    <script src="/webjars/vue/2.5.16/dist/vue.min.js"></script>
    <script src="/webjars/axios/0.17.1/dist/axios.min.js"></script>
    <script src="/webjars/sockjs-client/1.5.1/sockjs.min.js"></script>
    <script src="/webjars/stomp-websocket/2.3.4/stomp.min.js"></script>
    <script>
        var login = new Vue({
            el: '#login',
            data: {
            	blogin : [],
                user_id : '',
                user_password : ''
            },
            created() {

            },
            methods: {
                signin: function() {
                    if("" === this.user_id) {
                        alert("로그인 아이디를 입력해 주십시요.");
                        return;
                    } else if("" === this.user_password) {
                    	alert("로그인 패스워드를 입력해 주십시요.");
                        return;
                    } else {
                        var payload = {
                        	"user_id" : this.user_id,
                        	"user_password" : this.user_password
                        };
    
                        axios.post('/user/signin', JSON.stringify(payload), {
			            	headers : {
			                    'Content-Type' : 'application/json;charset=utf-8'
			                }
                        })
                        .then(
                            response => {
                                alert("로그인에 성공하였습니다.");
                                this.blogin = null;
                                
                                var type = response.data.type;
                                var token = response.data.token;
                                var accessToken = type + ' ' + token;
                                var userId = response.data.user_id;
                                
                                
                                var chatRoom = new Vue({
						            el: '#chatroom',
						            data: {
						            	bchatroom : [],
						                room_name : '',
						                chatrooms: []
						            },
						            created() {
						                this.findAllRoom();
						            },
						            methods: {
						                findAllRoom: function() {
						                    axios.get('/chat/rooms', {
								            	headers : {
								                    'Content-Type' : 'application/json;charset=utf-8',
								                    'Authorization' : accessToken,
								                }
						                    })
						                    .then(response => {
						                        // prevent html, allow json array
						                        if(Object.prototype.toString.call(response.data) === "[object Array]")
						                            this.chatrooms = response.data;
						                    });
						                },
						                createRoom: function() {
						                    if("" === this.room_name) {
						                        alert("방 제목을 입력해 주십시요.");
						                        return;
						                    } else {
						                       var payload = {
						                       		"name" : this.room_name
						                       };
						                       axios.post('/chat/room', JSON.stringify(payload), {
									            	headers : {
									                    'Content-Type' : 'application/json;charset=utf-8',
									                    'Authorization' : accessToken
									                }
						                        })
						                        .then(
						                            response => {
						                                alert(response.data.name+"방 개설에 성공하였습니다.")
						                                this.room_name = '';
						                                this.findAllRoom();
						                            }
						                        )
						                        .catch( response => { alert("채팅방 개설에 실패하였습니다."); } );
						                    }
						                },
						                enterRoom: function(roomId, roomName) {		
						                	this.bchatroom = null;
						               		bchat = [];
									        // websocket & stomp initialize
									        var sock = new SockJS("/seers");
									        var ws = Stomp.over(sock);
									        // vue.js
									        var chat = new Vue({
									            el: '#chat',
									            data: {
									                roomId: '',
									                roomName: '',
									                message: '',
									                messages: [],
									                token: '',
									                userCount: 0
									            },
									            created() {
									                this.roomId = roomId;//localStorage.getItem('wschat.roomId');
									                this.roomName = roomName;//localStorage.getItem('wschat.roomName');
									                var _this = this;
									                
								                    ws.connect({"token":accessToken}, function(frame) {
								                        ws.subscribe("/sub/chat/room/"+_this.roomId, function(message) {
								                            var recv = JSON.parse(message.body);
								                            _this.recvMessage(recv);
								                        });
								                    }, function(error) {
								                        alert("서버 연결에 실패 하였습니다. 다시 접속해 주십시요.");
								                        location.href="/chat/room";
								                    });
									            },
									            methods: {
									                sendMessage: function(type) {
									                    ws.send("/pub/chat/message", {"token":accessToken}, JSON.stringify({type:type, roomId:this.roomId, message:this.message}));
									                    this.message = '';
									                },
									                recvMessage: function(recv) {
									                    this.userCount = recv.userCount;
									                    this.messages.unshift({"type":recv.type,"sender":recv.sender,"message":recv.message})
									                }
									            }
									        });						                	
						                
						                    //localStorage.setItem('wschat.roomId',roomId);
						                    //localStorage.setItem('wschat.roomName',roomName);
						                    //location.href="/chat/room/enter/"+roomId;
						                }
						            }
						        });
                                
                            }
                        )
                        .catch( response => { alert("로그인에 실패하였습니다."); } );
                    }
                }
            }
        });
           
    </script>
  </body>
</html>
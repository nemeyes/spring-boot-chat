<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8">
    	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    	<meta name="description" content="">
    	<meta name="author" content="">
    	<title>Please sign in</title>
    	<link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M" crossorigin="anonymous">
    	<link href="https://getbootstrap.com/docs/4.0/examples/signin/signin.css" rel="stylesheet" crossorigin="anonymous"/>
	</head>
	<body>
    	<div class="container" id="app">
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
		</div>
		<!-- JavaScript -->
	    <script src="/webjars/vue/2.5.16/dist/vue.min.js"></script>
	    <script src="/webjars/axios/0.17.1/dist/axios.min.js"></script>
	    <script>
	        var vm = new Vue({
	            el: '#app',
	            data: {
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
	                                alert("로그인에 성공하였습니다.")
	                                //this.room_name = '';
	                                //this.findAllRoom();
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
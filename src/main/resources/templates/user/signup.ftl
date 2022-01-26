<!DOCTYPE html>
<html lang="en">
  	<head>
	    <title>SeersChat</title>
	    <meta charset="utf-8">
	    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
	    <!-- CSS -->
	    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/dist/css/bootstrap.min.css">
  	</head>
    <body>
    	<div class="container" id="app">
			<h2 class="form-signin-heading">Please sign up</h2>
			<p>
				<label for="username" class="sr-only">Login ID</label>
				<input type="text" class="form-control" v-model="user_id" placeholder="User ID" required autofocus/>
			</p>
    		<p>
      			<label for="password" class="sr-only">Login Password</label>
      			<input type="password" class="form-control" v-model="user_password" placeholder="User Password" required/>
    		</p>
    		<p>
      			<label for="username" class="sr-only">Nick Name</label>
      			<input type="username" class="form-control" v-model="user_nickname" placeholder="User Nickname" required/>
    		</p>
    		<button class="btn btn-lg btn-primary btn-block" @click="register">가입 하기</button>
		</div>
    <!-- JavaScript -->
    <script src="/webjars/vue/2.5.16/dist/vue.min.js"></script>
    <script src="/webjars/axios/0.17.1/dist/axios.min.js"></script>
    <script>
        var vm = new Vue({
            el: '#app',
            data: {
                user_id : '',
                user_password : '',
                user_nickname : ''
            },
            created() {

            },
            methods: {
                register: function() {
                    if("" === this.user_id) {
                        alert("로그인 아이디를 입력해 주십시요.");
                        return;
                    } else if("" === this.user_password) {
                    	alert("로그인 패스워드를 입력해 주십시요.");
                        return;
                    } else if("" === this.user_nickname) {
                    	alert("로그인 닉네임을 입력해 주십시요.");
                        return;	                        
                    } else {
                        var payload = {
                        	"user_id" : this.user_id,
                        	"user_password" : this.user_password,
                        	"user_nickname" : this.user_nickname,
                        	"user_roles" : [
                        		"user"
                        	]
                        };
    
                        axios.post('/user/signup', JSON.stringify(payload), {
			            	headers : {
			                    'Content-Type' : 'application/json;charset=utf-8'
			                }
                        })
                        .then(
                            response => {
                                alert("가입에 성공하였습니다.")
                                location.href="/chat/room";
                            }
                        )
                        .catch( response => { alert("가입에 실패하였습니다."); } );
                    }
                }
            }
        });
    </script>
    </body>
</html>
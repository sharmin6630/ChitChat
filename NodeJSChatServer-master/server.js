const express = require('express'),
http = require('http'),
app = express(),
server = http.createServer(app),
io = require('socket.io').listen(server);

//database setup

const mongoose = require('mongoose')
const validator = require('validator')
mongoose.connect('mongodb://127.0.0.1:27017/ChitChat', {
  useNewUrlParser: true,
  useCreateIndex: true
});

// user entity

const chatUser = mongoose.model('chatUser', {
    uniqueId: {
      type: String,
      trim: true
    },
    userName: {
      type: String,
      trim: true
    },
    message: {
      type: String,
      trim: true
    }
  
});

// active user entity

const activeList = mongoose.model('activeList', {
  uniqueId: {
      type: String,
      trim: true
  },
  userName: {
      type: String,
      trim: true
  }
});

app.use(express.json())
app.get('/chatUser', (req, res) => {
  chatUser.find({}).then((chats) => {
    res.send(chats);
  }).catch((e) => {
    console.log(e);
  });
});

app.get('/', (req, res) => {
    res.send('Chat Server is running on port 3000')
});

const online = {}

io.on('connection', (socket) => {

  console.log('user connected')
  

  socket.on('join', function(userNickname) {

          online[socket.id] = userNickname

          const active = new activeList({
              uniqueId: socket.id,
              userName: userNickname
          });
          active.save().then(() => {
              console.log(active)
          }).catch((error) => {
              console.log('Error!', error)
          });

          console.log(userNickname +" : has joined the chat "  );

          socket.broadcast.emit('userjoinedthechat',userNickname +" : has joined the chat ");
  8});


  socket.on('messagedetection', (senderNickname,messageContent) => {

      const messageLog = new chatUser({
          uniqueId: senderNickname,
          userName: senderNickname,
          message: messageContent
      });
      messageLog.save().then(() => {
          console.log(messageLog)
      }).catch((error) => {
          console.log('Error!', error)
      });
        
        //log the message in console 

        console.log(senderNickname+" :" +messageContent)
          //create a message object 
        let  message = {"message":messageContent, "senderNickname":senderNickname}
            // send the message to the client side  
        io.emit('message', message );
      
        });
        
  socket.on('on typing', function(typing){
    io.emit('on typing', typing);
  });
        
  // socket.on('disconnect', function() {
  //     console.log( ' user has left ')
  //     socket.broadcast.emit("userdisconnect"," user has left ") 

  // });

  socket.on('load_previous_msg', (senderNickname) => {

      chatUser.find({}).then((history)=>{

          var i=0;
          while(i<history.length){
              var id = history[i].uniqueId
              var name = history[i].userName
              var msg = history[i].message

              console.log(name+" "+msg);

              console.log(senderNickname+"ache ki na");
              //create a message object 
            let  message = {"message":msg, "senderNickname":name}
                // send the message to the client side  
            io.to(`${socket.id}`).emit('message', message );
            i++;
          };
        
      }).catch((error)=>{
          console.log(error);
      });



    
    });
      
      socket.on('on typing', function(typing){
        io.emit('on typing', typing);
      });
      
  socket.on('disconnect', function() {
        var userName = online[socket.id]
        console.log(userName)
        activeList.deleteOne({
            uniqueId : socket.id
        }).then ( (result) => {
            console.log(result)
        }).catch((e)=>{
            console.log(e)
        });
        delete online[socket.id]
        console.log( ' user has left ')
        socket.broadcast.emit("userdisconnect",userName+ " : has left ") 
        //socket.broadcast.emit('userdisconnect',senderNickname +" : has left the chat ");

  });

});

server.listen(3000,()=>{

console.log('Node app is running on port 3000');

});
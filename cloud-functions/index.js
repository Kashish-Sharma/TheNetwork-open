'use-strict'
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
exports.sendNotification = functions.firestore.document("Users/{user_id}/Notifications/{notification_id}").onWrite((change,context)=> {
 
 const user_id = context.params.user_id;
 const notification_id = context.params.notification_id;
 
 console.log("USER ID : "+user_id+" NOTIFICATION ID "+notification_id);
 
 return admin.firestore().collection("Users").doc(user_id).collection("Notifications").doc(notification_id).get().then(queryResult =>{
  
  const from_user_id = queryResult.data().from;
  const type = queryResult.data().type;
  
  console.log("FROM_USER ID : "+from_user_id+" TYPE "+type);
  
  const from_data = admin.firestore().collection("Users").doc(from_user_id).get();
  const to_data = admin.firestore().collection("Users").doc(user_id).get();
  
  
  return Promise.all([from_data, to_data]).then(result => {
   
   const from_name = result[0].data().name;
   const to_name = result[1].data().name;
   const token_id = result[1].data().token_id;
   
   const payload = {
    notification: {
     tag : from_user_id+"Follow",
     title : "Follow request",
     icon  : "action_normal_like",
     color : "white",
     sound : "TYPE_NOTIFICATION",
     body  : from_name+" has requested to follow you.",
     click_action:"com.thenetwork.app.android.thenetwork.Activities.ONFOLLOWREQUESTRECEIVED"
    },
    data : {
     message : from_name+" has requested to follow you.",
     user_id : from_user_id
    }
   };
   
   return admin.messaging().sendToDevice(token_id, payload).then(result => {
    
    var db = admin.firestore();
    
    const FieldValue = require('firebase-admin').firestore.FieldValue;
    
    var notificationRef = db.collection("Users").doc(user_id).collection("Notifications").doc(notification_id).delete();
    
    return console.log("Follow notification sent");
    
   });
   
  });
  
 });
 
}); 
exports.sendNotificationOnNewChatMessage = functions.firestore.document("Messages/{user_id}/{with_user_id}/{message_id}").onWrite((change,context)=>{
  const user_id = context.params.user_id;
  const with_user_id = context.params.with_user_id;
  const message_id = context.params.message_id;
  
  console.log("USER ID : "+user_id+" : WITH_USER ID : "+with_user_id+" : MESSAGE_ID : "+message_id);
  
  var db = admin.firestore();
  const messageFromOtherUserRef = db.collection("Messages").doc(user_id).collection(with_user_id);
  
  const unreadMessagesFromOtherUserQuery = messageFromOtherUserRef.where('seen','==',false).where('from','==',with_user_id);
  
 
  
  unreadMessagesFromOtherUserQuery.get().then(querySnapshot => {
   
   if(querySnapshot.size > 0){
    
    const newMessageCount = querySnapshot.size;
    const current_user_data = admin.firestore().collection("Users").doc(user_id).get();
    const to_user_data = admin.firestore().collection("Users").doc(with_user_id).get();
    
    return Promise.all([current_user_data, to_user_data]).then(result => {
   
     const current_name = result[0].data().name;
     const to_name = result[1].data().name;
     const token_id = result[0].data().token_id;
     const to_image = result[1].data().image;
   
     const payload = {
      notification: {
       tag : user_id+"New Chat Message",
       title : "New Messages",
       icon  : "action_normal_like_white",
       color : "white",
       sound : "TYPE_NOTIFICATION",
       body  : "You have "+newMessageCount+" unread messages from "+to_name,
       click_action:"com.thenetwork.app.android.thenetwork.Activities.ONNEWMESSAGERECEIVED"
      },
      data : {
       message : "You have "+newMessageCount+" unread messages from "+to_name,
       user_id : with_user_id,
       image : to_image,
       name : to_name
      }
     };
   
     return admin.messaging().sendToDevice(token_id, payload).then(result => {
    
      return console.log("New chat notification sent");
    
     });
   
   });
    
    
    
    
   } else{
    return console.log("no such documents exists ");
   }
   
  })
  .catch(err => {
   console.log("Error getting documents ",err);
  })
        
});
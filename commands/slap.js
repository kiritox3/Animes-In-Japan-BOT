const Discord = require("discord.js");

exports.run = async(client, message, args) => {
  var list = [
    'https://i.imgur.com/fhu8qdo.gif',
    'https://i.imgur.com/LjlQTwt.gif',
    'https://i.imgur.com/D55G1Mr.gif',
    'https://i.imgur.com/gRoKPBk.gif',
    'https://i.imgur.com/Sh0emX6.gif',
    'https://i.imgur.com/Ytcji1M.gif',
    'https://i.imgur.com/RCsIo1x.gif',
    'https://i.imgur.com/4MeCuQk.gif',
    'https://i.imgur.com/cY78Fad.gif',
  
  ];

  var rand = list[Math.floor(Math.random() * list.length)];
  let member = message.mentions.users.first() || client.users.cache.get(args[0]);
  
  if (!member) return message.channel.send(`<a:aaaa:853512123002126366>| Mencione uma pessoa para dar um tapa.`);
  message.delete().catch(O_o => {});
  let hug = new Discord.MessageEmbed()

  .setDescription(`<a:DIOO:873317556071563316> ${message.author} **deu um tapa em** ${member}!`)
  .setImage(rand)
  .setTimestamp()
  .setColor("#e300ff")
  
  .setFooter(`a!slap`, message.author.displayAvatarURL({format:"png"}));

  message.channel.send(hug)
  
  }
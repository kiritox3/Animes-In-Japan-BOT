const Discord = require("discord.js");

exports.run = async(client, message, args) => {
  var list = [
    'https://i.imgur.com/vKldDsS.gif',
    'https://i.imgur.com/I3LVbcw.gif',
    'https://i.imgur.com/63cDwA0.gif',
    'https://i.imgur.com/gb4A7wo.gif',
    'https://i.imgur.com/xsPMAgY.gif',
    'https://i.imgur.com/M3CsQCS.gif',
    'https://i.imgur.com/TI1LPTq.gif',
    'https://i.imgur.com/zEAz8i9.gif',
    'https://i.imgur.com/PKfnvA0.gif',
    'https://i.imgur.com/RdFtMmG.gif',
  ];

  var rand = list[Math.floor(Math.random() * list.length)];
  let member = message.mentions.users.first() || client.users.cache.get(args[0]);
  
  if (!member) return message.channel.send(`<a:aaaa:853512123002126366>| Mencione uma pessoa para abraçar.`);
  message.delete().catch(O_o => {});
  let hug = new Discord.MessageEmbed()

  .setDescription(`<:nicee:873135197523152906> ${message.author} **abraçou** ${member}!`)
  .setImage(rand)
  .setTimestamp()
  .setColor("#e300ff")
  
  .setFooter(`a!hug`, message.author.displayAvatarURL({format:"png"}));

  message.channel.send(hug)
  
  }
const Discord = require("discord.js");

exports.run = async(client, message, args) => {
  var list = [
    'https://i.imgur.com/n2m5ETV.gif',
    'https://i.imgur.com/PQXSXrj.gif',
    'https://i.imgur.com/ihipkbh.gif',
    'https://i.imgur.com/jhBmija.gif',
    'https://i.imgur.com/h7nKlJi.gif',
    'https://i.imgur.com/1flzjB1.gif',
    'https://i.imgur.com/fTxPR9m.gif',
    'https://i.imgur.com/qPpyaSG.gif',
    'https://i.imgur.com/xhDNHHU.gif',
    'https://i.imgur.com/L2vMk61.gif',
    'https://i.imgur.com/1CWQMdi.gif',
    'https://i.imgur.com/r9AcS1d.gif',
    'https://i.imgur.com/gUwUmAN.gif',
    'https://i.imgur.com/iMSThNF.gif',
    'https://i.imgur.com/iEtb3ne.gif',
   
  ];

  var rand = list[Math.floor(Math.random() * list.length)];
  let member = message.mentions.users.first() || client.users.cache.get(args[0]);
  
  if (!member) return message.channel.send(`<a:aaaa:853512123002126366>| Mencione uma pessoa para beijar.`);
  message.delete().catch(O_o => {});
  let hug = new Discord.MessageEmbed()

  .setDescription(`<a:kiss:873328381838635038> ${message.author} **beijou** ${member}!`)
  .setImage(rand)
  .setTimestamp()
  .setColor("#ffb500")
  
  .setFooter(`a!kiss`, message.author.displayAvatarURL({format:"png"}));

  message.channel.send(hug)
  
  } 
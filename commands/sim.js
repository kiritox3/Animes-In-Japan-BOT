const Discord = require('discord.js');

exports.run = async (bot, message, args) => {

  const Embed = new Discord.MessageEmbed()
  .setTitle(`usa sas coisa n`)
  .setDescription(`O chat é bom, ajuda de mais`)
  .setColor('RANDOM')
  .setTimestamp()
  message.channel.send(Embed).then(msg => {
    msg.react('😔')
  })
};
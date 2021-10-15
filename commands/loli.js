const Discord = require('discord.js');

exports.run = async (bot, message, args) => {
  const sayMessage = args.join(' ');
  message.delete().catch(O_o => {});
  message.channel.send(" https://c.tenor.com/ZvMZoQy5hkMAAAAC/fbi-fbi-open-the-door.gif Hello fbi??");
};
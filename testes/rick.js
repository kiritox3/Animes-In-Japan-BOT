const Discord = require('discord.js');
const player = require('discord-music-player');

exports.run = async(client, message, args) => {

  let guildQueue = client.player.getQueue(message.guild.id);
  
  let m = args.join(' ');
let queue = client.player.createQueue(message.guild.id);
  await queue.join(message.member.voice.channel);
let song = await queue.play(m).catch(_ => 
{
  if(!guildQueue)
  queue.stop()

}) 
  const u = message.member.voice.channel.id
  if(!u) { message.channel.send('**Precisa estar ne um canal de voz.**') 
         }


let card = new Discord.MessageEmbed()
.setColor("#EE82EE")
.setTitle(`**${message.author.username}**`)
.setImage(`${song.thumbnail}`)
.setDescription(`Musica: [${song.name}](${song.url})\n Artista: ${song.author}`)
await message.channel.send({ embeds: [card] });
};

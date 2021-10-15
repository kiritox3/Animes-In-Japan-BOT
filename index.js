const Discord = require("discord.js");
const config = require("./config.json");
const bot = new Discord.Client();
const express = require("express");

















const { Player } = require('discord-music-player');
const player = new Player(bot, {
  LeaveOnEmply: false,
  
})

bot.player = player;

const app = express();
app.get("/", (request, response) => {
  const ping = new Date();
  ping.setHours(ping.getHours() - 3);
  console.log(`Ping veio, olha a hora ${ping.getUTCHours()}:${ping.getUTCMinutes()}:${ping.getUTCSeconds()}`);
  response.sendStatus(200);
});
app.listen(process.env.PORT); // Recebe solicitações que o deixa online


//inicio de tudo
bot.on('message', message => {
  if (message.author.bot) return;
  if (message.channel.type == 'channel') return;
  if (!message.content.toLowerCase().startsWith(config.prefix)) return;
  if (message.content.startsWith(`<@!${bot.user.id}>`) || message.content.startsWith(`<@${bot.user.id}>`)) return;

  const args = message.content
    .trim().slice(config.prefix.length)
    .split(/ +/g); 
  const command = args.shift().toLowerCase();

  try {
    const commandFile = require(`./commands/${command}.js`)
   
    
    //puxando a pasta comands + o comando
commandFile.run(bot, message, args);
  } catch (err) {
    const embed = new Discord.MessageEmbed()
    
    .setColor('RANDOM')
    .setTitle(`❌| Comando inexistente!`)
    .setDescription(`Estamos trabalhando nisso.`)
    return message.channel.send(embed);
    return message.channel.send(embed);
  }
});
 




//Welcomer Teste
bot.on('guildMemberAdd', async member => {
  let guild = await bot.guilds.cache.get('850533070708932639');
  let channel = await bot.channels.cache.get('851965822758944798');
  if (guild != member.guild) {
    return console.log('Entrou');
  } else {
    
    let embed = await new Discord
  
    .MessageEmbed()
      .setColor('#00caff')
      .setAuthor(member.user.tag, member.user.displayAvatarURL())
      .setThumbnail(member.user.displayAvatarURL({ dynamic: true, format: "png", size: 1024 }))
      .setTitle(`<a:bolarebola:873328466374852608>| Bem-vindo(a)!`)
      .setFooter(`ID do usuário: ${member.user.id}`)
      .setImage('https://i.imgur.com/pVErgUa.gif')
      .setDescription(
        `     <:nicee:873135197523152906>| **Leia as regras!** \n <#808154560068583514> \n<a:DIOO:873317556071563316>|  **Registre-se** \n  <#878475977360044033>
                `

      )
      
    channel.send(embed);
   
  }
});

//leave teste
bot.on("guildMemberRemove", async (member) => {

  let guild = await bot.guilds.cache.get("850533070708932639");
  let channel = await bot.channels.cache.get("851965822758944798");
  if (guild != member.guild) {
    return console.log('Saiu');
  } else {
    let embed = await new Discord.MessageEmbed()
      .setColor("#000001")
      .setAuthor(member.user.tag, member.user.displayAvatarURL())
      .setTitle(`<a:nime_cry2:853512070594691098>| Até Mais!`)
      .setImage('https://imgur.com/tcXFHWw.gif')
      .setDescription(`<a:taigahh:853511913563488298>  | **${member.user.username}** saiu do servidor. \n Agora estamos com **${member.guild.memberCount}** membros! `)
      .setThumbnail(member.user.displayAvatarURL({ dynamic: true, format: "png", size: 1024 }))
      .setTimestamp();

    channel.send(embed);
  }
});

//Welcomer AIJ
bot.on('guildMemberAdd', async member => {
  let guild = await bot.guilds.cache.get('804102103911497738');
  let channel = await bot.channels.cache.get('853443495293747240');
  if (guild != member.guild) {
    return console.log('Entrou');
  } else {
    
    let embed = await new Discord
  
    .MessageEmbed()
      .setColor('#00caff')
      .setAuthor(member.user.tag, member.user.displayAvatarURL())
      .setThumbnail(member.user.displayAvatarURL({ dynamic: true, format: "png", size: 1024 }))
      .setTitle(`<a:bolarebola:873328466374852608>| Bem-vindo(a)!`)
      .setFooter(`ID do usuário: ${member.user.id}`)
      .setImage('https://i.imgur.com/pVErgUa.gif')
      .setDescription(
        `     <a:crazy_nime:853449939069698048>| **Leia as regras!** \n <#808154560068583514> \n<a:DIOO:873317556071563316>|  **Registre-se** \n  <#878475977360044033>
                `

      )
      
    channel.send(embed);
   
  }
});

//Leave AIJ
bot.on("guildMemberRemove", async (member) => {

  let guild = await bot.guilds.cache.get("804102103911497738");
  let channel = await bot.channels.cache.get("853443667506626571");
  if (guild != member.guild) {
    return console.log('Saiu');
  } else {
    let embed = await new Discord.MessageEmbed()
      .setColor("#000001")
      .setAuthor(member.user.tag, member.user.displayAvatarURL())
      .setTitle(`<a:nime_cry2:853512070594691098>| Até Mais!`)
      .setImage('https://imgur.com/tcXFHWw.gif')
      .setDescription(`<a:taigahh:853511913563488298> | **${member.user.username}** saiu do servidor. \n\n Agora estamos com **${member.guild.memberCount}** membros! `)
      .setThumbnail(member.user.displayAvatarURL({ dynamic: true, format: "png", size: 1024 }))
      .setTimestamp();

    channel.send(embed);
  }
});





//status
bot.on('ready', () => {
  console.log('Tamo aqui!!');
  var tabela = [
    { name: 'Estou aqui!', type: 'PLAYING' },
    { name: 'a!help', type: 'WATCHING' }
  ];

  function setStatus() {
    var altstatus = tabela[Math.floor(Math.random() * tabela.length)]
    bot.user.setActivity(altstatus)
  }
  setStatus("online")
  setInterval(() => setStatus(), 5000)
})

bot.login(config.token);
const Discord = require("discord.js");

exports.run = (client, message, args) => {
    
    const embed = new Discord.MessageEmbed()
    .setColor('#08ddf5')
    .setDescription(`**Commands:** `)
    .setTimestamp()
    .setFooter(` Autor: ${message.author.username} `)
    .addFields(
        {
            name: 'a!avatar',
            value: `Exibe o avatar do usuário mencionado.`,
            inline: true
        },
        
        {
            name:'a!hug',
            value: `Abraça o usuário mencionado.`,
            inline: true
        },
        
        {
          name:'a!kiss',
          value: `Beija o usuário mencionado.`,
          inline: true
        },

        {
          name:'a!slap',
          value: `Da um tapa no usuário mencionado.`,
          inline: true
        }, 
        
        {
          name:'a!botinfo',
          value: `Apresenta informações sobre eu.`,
          inline: true
        },
            
        {
          name:'a!ping',
          value: `Exibe o ms da bot.`,
          inline: true
        },

           {
          name:'a!lock (mod)',
          value: `Fecha o canal.`,
          inline: true
        },

        {
          name:'a!unlock (mod)',
          value: `Abre o canal.`,
          inline: true
        },
        
        
        {
          name:'a!embed (admin)',
          value: `Repete o que você digitou em uma embed.`,
          inline: true
        },
        
        
    )
    message.delete().catch(O_o => {});
    message.channel.send(embed);
}
package fr.yukina.game.graphic;

import fr.yukina.game.graphic.opengl.shader.ShaderProgram;
import fr.yukina.game.logic.Transformations;
import fr.yukina.game.logic.scene.Scene;
import org.joml.Vector3f;

public class Renderer
{
	private final Scene scene;

	public Renderer(Scene sceneIn)
	{
		this.scene = sceneIn;
	}

	public void render(ShaderProgram shaderProgramIn)
	{
		shaderProgramIn.attach();
		shaderProgramIn.uniform("pointLightPosition", this.scene.pointLight().position());
		shaderProgramIn.uniform("pointLight.color", this.scene.pointLight().color());
		shaderProgramIn.uniform("pointLight.intensity", this.scene.pointLight().intensity());
		shaderProgramIn.uniform("pointLight.attenuation.constant", this.scene.pointLight().attenuation().constant());
		shaderProgramIn.uniform("pointLight.attenuation.linear", this.scene.pointLight().attenuation().linear());
		shaderProgramIn.uniform("pointLight.attenuation.exponent", this.scene.pointLight().attenuation().exponent());
		shaderProgramIn.uniform("ambientLight", new Vector3f(1.0f, 1.0f, 1.0f));
		shaderProgramIn.uniform("specularPower", 20.0f);
		shaderProgramIn.uniform("projection", this.scene.player().camera().projectionMatrix());
		shaderProgramIn.uniform("view", this.scene.player().camera().viewMatrix());
		this.scene.items().forEach((mesh, items) ->
		                           {
			                           items.forEach(item ->
			                                         {
				                                         shaderProgramIn.uniform("material.color",
				                                                                 item.material().color());
				                                         shaderProgramIn.uniform("material.diffuseColor",
				                                                                 item.material().diffuseColor());
				                                         shaderProgramIn.uniform("material.specularColor",
				                                                                 item.material().specularColor());
				                                         shaderProgramIn.uniform("material.reflectance",
				                                                                 item.material().reflectance());
				                                         shaderProgramIn.uniform("transformation",
				                                                                 Transformations.of(item));
				                                         mesh.attach();
				                                         mesh.draw();
				                                         mesh.detach();
			                                         });
		                           });
		this.scene.entities().forEach((mesh, entities) ->
		                              {
			                              entities.forEach(entity ->
			                                               {
				                                               shaderProgramIn.uniform("material.color",
				                                                                       entity.material().color());
				                                               shaderProgramIn.uniform("material.diffuseColor",
				                                                                       entity.material()
				                                                                             .diffuseColor());
				                                               shaderProgramIn.uniform("material.specularColor",
				                                                                       entity.material()
				                                                                             .specularColor());
				                                               shaderProgramIn.uniform("material.reflectance",
				                                                                       entity.material().reflectance());
				                                               shaderProgramIn.uniform("transformation",
				                                                                       Transformations.of(entity));
				                                               mesh.attach();
				                                               mesh.draw();
				                                               mesh.detach();
			                                               });
		                              });
		shaderProgramIn.detach();
	}

	public void cleanup()
	{
		this.scene.items().forEach((mesh, items) -> mesh.cleanup());
	}
}

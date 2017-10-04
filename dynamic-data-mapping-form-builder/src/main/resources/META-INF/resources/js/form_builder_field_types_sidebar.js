AUI.add(
	'liferay-ddm-form-builder-field-types-sidebar',
	function(A) {
		var CSS_PREFIX = A.getClassName('form', 'builder', 'field', 'types', 'sidebar');

		var FieldTypes = Liferay.DDM.Renderer.FieldTypes;

		var FormBuilderFieldTypesSidebar = A.Component.create(
			{
				ATTRS: {
					builder: {
						value: null
					},
					fieldTypes: {
						getter: '_getFieldTypes'
					},
					strings: {
						value: {
							basic: Liferay.Language.get('field-types-basic-elements'),
							customized: Liferay.Language.get('field-types-customized-elements'),
							field: Liferay.Language.get('field'),
							fieldset: Liferay.Language.get('fieldset')
						}
					}
				},

				CSS_PREFIX: CSS_PREFIX,

				EXTENDS: Liferay.DDM.FormBuilderSidebar,

				NAME: 'liferay-ddm-form-builder-field-types-sidebar',

				prototype: {
					bindUI: function() {
						var instance = this;

						FormBuilderFieldTypesSidebar.superclass.bindUI.apply(instance, arguments);

						var boundingBox = instance.get('boundingBox');
						var navLink = '.' + CSS_PREFIX + ' .nav-link';

						instance._eventHandlers.push(
							A.one('body').delegate('click', instance._bindTabAnimation.bind(instance), navLink),
							boundingBox.delegate('click', instance._afterFieldTypeItemClick.bind(instance), '.list-field-type-item')
						);

						new A.TogglerDelegate(
							{
								animated: true,
								closeAllOnExpand: true,
								container: '.' + CSS_PREFIX + ' .field-types-content',
								content: '.list-group-body',
								expanded: false,
								header: '.list-group-header',
								transition: {
									duration: 0.2,
									easing: 'cubic-bezier(0, 0.1, 0, 1)'
								}
							}
						);
					},

					destructor: function() {
						var instance = this;

						(new A.EventHandle(instance._eventHandlers)).detach();
					},

					getTemplateContext: function() {
						var instance = this;

						var context = FormBuilderFieldTypesSidebar.superclass.getTemplateContext.apply(instance, arguments);

						return A.merge(
							context,
							{
								fieldTypes: instance.get('fieldTypes'),
								strings: instance.get('strings'),
								title: Liferay.Language.get('field-types-sidebar-title'),
								type: 'fieldTypes'
							}
						);
					},

					hasFocus: function(node) {
						var instance = this;

						var activeElement = A.one(node || document.activeElement);

						return instance.get('boundingBox').contains(activeElement);
					},

					_afterFieldTypeItemClick: function(event) {
						var instance = this;
						var item = event.currentTarget;
						var result = FieldTypes.getAll().filter(
							function(fieldType) {
								return fieldType.get('name') === item.attr('data-field-type-name');
							}
						);

						instance.get('builder').createNewField(result[0]);
					},

					_afterPressEscapeKey: function() {
						var instance = this;

						if (instance.isOpen()) {
							instance.close();
						}
					},

					_bindTabAnimation: function(e) {
						var instance = this;
						var item = e.target;

						if (item.hasClass('active')) {
							return;
						}

						var oldItem = A.one('.' + CSS_PREFIX + ' .nav-link.active');
						var oldTarget = A.one('.' + oldItem.attr('data-tab'));
						var target = A.one('.' + item.attr('data-tab'));

						oldItem.removeClass('active');
						oldTarget.hide();

						item.addClass('active');
						target.show();
					},

					_getFieldTypes: function(fieldTypes) {
						var instance = this;

						var types = [];

						fieldTypes.forEach(
							function(fieldType) {
								types.push(
									{
										description: fieldType.get('description'),
										group: fieldType.get('group') || 'customized',
										icon: window.DDMFieldTypesSidebar.render.Soy.toIncDom(Liferay.Util.getLexiconIconTpl(fieldType.get('icon'), 'icon-monospaced')),
										label: fieldType.get('label'),
										name: fieldType.get('name')
									}
								);
							}
						);

						return _.groupBy(types, 'group');
					}
				}
			}
		);

		Liferay.namespace('DDM').FormBuilderFieldTypesSidebar = FormBuilderFieldTypesSidebar;
	},
	'',
	{
		requires: ['aui-tabview', 'aui-toggler', 'liferay-ddm-form-builder-sidebar', 'liferay-ddm-form-renderer-types']
	}
);
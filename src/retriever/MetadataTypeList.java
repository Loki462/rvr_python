/**
 * 
 */
package retriever;

/**
 * @author Debdatta Porya
 *
 */
public class MetadataTypeList {
	public enum CustomSettingsChange{
		AccountSettings,Dashboard,LiveChatAgentConfig,
		AccountRelationshipShareRule,DataCategoryGroup,LiveChatButton,
		ActionLinkGroupTemplate,Document,LiveChatDeployment,
		ActionOverride,EclairGeoData,LiveMessageSettings,
		ActivitiesSettings,EmailServicesFunction,MacroSettings,
		AddressSettings,EmailTemplate,ManagedTopics,
		AnalyticSnapshot,EmbeddedServiceBranding,Metadata,
		AppMenu,EmbeddedServiceConfig,MetadataWithContent,
		ArticleType,EmbeddedServiceFlowConfig,MilestoneType,
		Bot,EmbeddedServiceLiveAgent,MlDomain,
		BotVersion,	EntitlementSettings,MobileSettings,
		BrandingSet,EntitlementTemplate,NamedCredential,
		BusinessHoursSettings,EventDelivery,NamedFilter,
		CallCenter,EventSubscription,NameSettings,
		CaseSettings,ExternalServiceRegistration,Network,
		ChatterAnswersSettings,ExternalDataSource,NetworkBranding,
		ChatterExtension,FeatureParameterBoolean,OmniChannelSettings,
		CleanDataService,FeatureParameterDate,OpportunitySettings,
		CMSConnectSource,FeatureParameterInteger,OrderSettings,
		CompanySettings,FieldSet,OrgPreferenceSettings,
		Community,FlexiPage,Package,
		CommunityTemplateDefinition,Folder,PathAssistant,
		CommunityThemeDefinition,FolderShare,PathAssistantSettings,
		CompactLayout,ForecastingSettings,Picklist,
		ConnectedApp,GlobalValueSet,PlatformCachePartition,
		ContentAsset,GlobalValueSetTranslation,PlatformEventChannel,
		ContractSettings,GlobalPicklistValue,Portal,
		CorsWhitelistOrigin,HomePageComponent,PresenceDeclineReason,
		CustomApplication,HomePageLayout,PresenceUserConfig,
		CustomApplicationComponent,IdeasSettings,ProductSettings,
		CustomFeedFilter,Index,PostTemplate,CustomField,InstalledPackage,
		CustomHelpMenuSection,IoTSettings,QuickAction,
		CustomLabel,KeywordList,QuoteSettings,
		CustomObject,KnowledgeSettings,RecommendationStrategy,
		CustomMetadata,Layout,RecordActionDeployment,
		CustomLabels,Letterhead,RecordType,
		CustomObjectTranslation,LightningBolt,RemoteSiteSetting,
		CustomPageWebLink,LightningComponentBundle,	Report,
		CustomPermission,LightningExperienceTheme,ReportType,
		CustomSite,	ListView,SamlSsoConfig,
		CustomTab,LiveAgentSettings,Scontrol,
		SearchLayouts,SearchSettings,ServiceChannel,
		ServicePresenceStatus,SiteDotCom,Skill,
		SocialCustomerServiceSettings,StaticResource,SynonymDictionary,
		TopicsForObjects,Translations,WaveApplication,
		WaveDashboard,WaveDataflow,WaveDataset,
		WaveLens,WaveTemplateBundle,WaveXmd,WebLink
	}
	
	public enum ApexComponentChange{
		ApexClass,
		ApexComponent,
		ApexPage,
		ApexTestSuite,
		ApexTrigger
	}
	
	public enum BusinessLogicChanges{
		ApprovalProcess,
		AssignmentRules,
		AutoResponseRules,
		BaseSharingRule,
		BusinessProcess,
		CaseSubjectParticle,
		CriteriaBasedSharingRule,
		DuplicateRule,
		EntitlementProcess,
		Flow,
		FlowCategory,
		FlowDefinition,
		LiveChatSensitiveDataRule,
		MatchingRule,
		ModerationRule,
		ValidationRule,
		Workflow
	}
	
	public enum SecurityChanges{
		Audience,
		AuthProvider,
		AuraDefinitionBundle,
		Certificate,
		CspTrustedSite,
		DelegateGroup,
		FileUploadAndDownloadSecuritySettings,
		Group,
		Profile,
		ProfileActionOverride,
		ProfilePasswordPolicy,
		ProfileSessionSetting,
		Queue,
		QueueRoutingConfig,
		Role,
		PermissionSet,
		SecuritySettings,
		SharingBaseRule,
		OwnerSharingRule,
		SharingReason,
		SharingRecalculation,
		SharingRules,
		SharingSet,
		StandardValueSet,
		StandardValueSetTranslation,
		Territory,
		Territory2,
		Territory2Model,
		Territory2Rule,
		Territory2Settings,
		Territory2Type,
		TransactionSecurityPolicy
	}
}